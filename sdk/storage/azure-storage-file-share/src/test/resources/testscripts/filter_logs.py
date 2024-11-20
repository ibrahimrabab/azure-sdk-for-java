import re
from datetime import datetime

def extract_range(log_line):
    match = re.search(r'"x-ms-range":"bytes=(\d+)-(\d+)"', log_line)
    if match:
        return int(match.group(1)), int(match.group(2))
    return None

def extract_date(log_line):
    match = re.search(r'(\d{2} \w{3} \d{4} \d{2}:\d{2}:\d{2},\d{3})', log_line)
    if match:
        return datetime.strptime(match.group(1), '%d %b %Y %H:%M:%S,%f')
    return None

def filter_and_sort_logs(log_file_path, file_name, output_file_path):
    with open(log_file_path, 'r') as log_file:
        log_lines = log_file.readlines()

    # Regex pattern to match log lines that contain the file name
    pattern = re.compile(rf'\"FileName\":\".*{re.escape(file_name)}\"')

    # Filter log lines containing the specified file name
    filtered_log_lines = [line for line in log_lines if pattern.search(line)]

    # Group log lines by 4MB ranges
    log_lines_by_range = {}
    for line in filtered_log_lines:
        range_info = extract_range(line)
        if range_info:
            start_byte = range_info[0]
            range_key = start_byte // 4194304  # 4MB range
            if range_key not in log_lines_by_range:
                log_lines_by_range[range_key] = []
            log_lines_by_range[range_key].append(line)

    # Sort log lines within each range group by date
    sorted_log_lines = []
    for range_key in sorted(log_lines_by_range.keys()):
        log_lines_by_range[range_key].sort(key=extract_date)
        sorted_log_lines.extend(log_lines_by_range[range_key])
        sorted_log_lines.append("\n")  # Add a new line after each 4MB chunk


    # Write sorted log lines to the output file
    with open(output_file_path, 'w') as output_file:
        for line in sorted_log_lines:
            output_file.write(line)

    print(f"Filtered and sorted logs saved to {output_file_path}")

if __name__ == "__main__":
    log_file_path = "C:/azure-sdk-for-java/sdk/storage/azure-storage-file-share/target/azure-storage-file-share-test.log"
    file_name = "1_0_debug.txt"
    output_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/filtered_logs_1_0_debug.txt"

    # Call the filter_and_sort_logs function with the provided arguments
    filter_and_sort_logs(log_file_path, file_name, output_file_path)
