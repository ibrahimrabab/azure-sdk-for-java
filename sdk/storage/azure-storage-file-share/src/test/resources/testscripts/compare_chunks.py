import os
import sys

mismatch_found = False
EXPECTED_RANGED_GET_SIZE = 4 * 1024 * 1024  # 4MB

def print_expected_ranges(original_path):
    with open(original_path, "rb") as original_file:
        original_content = original_file.read()
        print("Expected ranges:")
        for i in range(0, len(original_content), EXPECTED_RANGED_GET_SIZE):
                expected_end = i + EXPECTED_RANGED_GET_SIZE - 1
                if i + EXPECTED_RANGED_GET_SIZE - 1 > len(original_content):
                    expected_end = len(original_content) - 1
                print(f"{i}-{expected_end}")

def compare_chunks(first_range, last_range, original_path, download_path, mismatch_start, mismatch_end):
    with open(original_path, "rb") as original_file:
        original_content = original_file.read()
        expected_content = original_content[first_range:last_range + 1]
        print(f"Expected range: {first_range}-{last_range}")
        #print(f"Expected bytes: {expected_content}")

    with open(download_path, "rb") as download_file:
        download_content = download_file.read()
        actual_content = download_content[mismatch_start:mismatch_end + 1]
        #print(f"Actual bytes: {actual_content}")

        print(actual_content == expected_content)

def compare_files(original_path, downloaded_path, output_path):
    global mismatch_found

    # Open the output file for writing
    with open(output_path, "w") as output_file:
        # Open both files in binary read mode
        with open(original_path, "rb") as original_file, open(downloaded_path, "rb") as downloaded_file:
            byte_position = 0
            chunk_size = 4 * 1024 * 1024  # 4MB

            while True:
                # Read a 4MB chunk from each file
                original_chunk = original_file.read(chunk_size)
                downloaded_chunk = downloaded_file.read(chunk_size)

                # Break the loop if end of either file is reached
                if not original_chunk or not downloaded_chunk:
                    break

                # Compare the chunks byte by byte
                mismatch_start = None
                for i in range(len(original_chunk)):
                    if original_chunk[i] != downloaded_chunk[i]:
                        if mismatch_start is None:
                            mismatch_start = byte_position + i
                        mismatch_end = byte_position + i
                        mismatch_found = True
                    elif mismatch_start is not None:
                        # Log the mismatch range
                        output_file.write(f"Mismatch found from byte: {mismatch_start} to byte: {mismatch_end}\n")
                        output_file.write(f"Original bytes: {original_chunk[mismatch_start - byte_position:mismatch_end - byte_position + 1]}\n")
                        output_file.write(f"Downloaded bytes: {downloaded_chunk[mismatch_start - byte_position:mismatch_end - byte_position + 1]}\n")
                        mismatch_start = None

                if mismatch_start is not None:
                    # Log the mismatch range if it extends to the end of the chunk
                    output_file.write(f"Mismatch found from byte: {mismatch_start} to byte: {mismatch_end}\n")
                    output_file.write(f"Original bytes: {original_chunk[mismatch_start - byte_position:mismatch_end - byte_position + 1]}\n")
                    output_file.write(f"Downloaded bytes: {downloaded_chunk[mismatch_start - byte_position:mismatch_end - byte_position + 1]}\n")

                # Increment the byte position for the next iteration
                byte_position += chunk_size

        # Final output if no mismatch is found
        if not mismatch_found:
            output_file.write("No mismatches found between the files.\n")

# Get the file paths from the command line arguments
original_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/original_data.txt"
downloaded_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/1_9.txt"
output_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/results_1_9.txt"

# Run the comparison
compare_chunks(25165824, 29360127, original_file_path, downloaded_file_path, 28450048, 32644352)
