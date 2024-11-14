# import os
# import sys
#
# mismatch_found = False
#
# def compare_files(original_path, downloaded_path, output_path):
#     global mismatch_found
#
#     # Open the output file for writing
#     with open(output_path, "w") as output_file:
#         # Open both files in binary read mode
#         with open(original_path, "rb") as original_file, open(downloaded_path, "rb") as downloaded_file:
#             byte_position = 0
#
#             while True:
#                 # Read a 1KB chunk from each file
#                 original_byte = original_file.read(1)
#                 downloaded_byte = downloaded_file.read(1)
#
#                 # Break the loop if end of either file is reached
#                 if not original_byte or not downloaded_byte:
#                     break
#
#                 # Compare the bytes
#                 if original_byte != downloaded_byte:
#                     mismatch_found = True
#                     # Write the mismatch information to the output file
#                     output_file.write(f"Mismatch found at byte: {byte_position}\n")
#                     output_file.write(f"Original byte: {original_byte} | Downloaded byte: {downloaded_byte}\n")
#                     return
#
#                 # Increment the byte position for the next iteration
#                 byte_position += 1
#
#         # Final output if no mismatch is found
#         if not mismatch_found:
#             output_file.write("No mismatches found between the files.\n")
#
# # Get the file paths from the command line arguments
# original_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/original_data.txt"
# downloaded_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/1_51.txt"
# output_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/results_1_51.txt"
#
# # Run the comparison
# compare_files(original_file_path, downloaded_file_path, output_file_path)

import os
import sys

mismatch_found = False

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
                for i in range(len(original_chunk)):
                    if original_chunk[i] != downloaded_chunk[i]:
                        mismatch_found = True
                        # Write the mismatch information to the output file
                        output_file.write(f"Mismatch found at byte: {byte_position + i}\n")
                        output_file.write(f"Original byte: {original_chunk[i]} | Downloaded byte: {downloaded_chunk[i]}\n")
                        break  # Skip to the next 4MB chunk

                # Increment the byte position for the next iteration
                byte_position += chunk_size

        # Final output if no mismatch is found
        if not mismatch_found:
            output_file.write("No mismatches found between the files.\n")

# Get the file paths from the command line arguments
original_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/original_data.txt"
downloaded_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/1_51.txt"
output_file_path = "C:/azure-sdk-for-java/contentmismatchrepro/results_1_51.txt"

# Run the comparison
compare_files(original_file_path, downloaded_file_path, output_file_path)
