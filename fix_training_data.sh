#!/bin/bash
sed 's/^ end\$//' training_data.txt > new_training_data.txt
sed '/^$/d' new_training_data.txt > new_training_data2.txt
#sed 's/.$//' training_data.txt > new_training_data.txt
