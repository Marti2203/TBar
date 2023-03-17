#!/bin/bash

output_path=$1
buggy_proj_path=$2
proj_name=$3

java -Xmx1g -cp "target/dependency/*" edu.lu.uni.serval.tbar.faultlocalization.FL $output_path $buggy_proj_path $proj_name
