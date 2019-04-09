#!/bin/bash
#sed 's/^ end\$//' training_data.txt > new_training_data.txt
#sed 's/end\$//' training_data.txt > new_training_data.txt
sed 's/$/ end\$/' training_data.txt > new_training_data.txt
