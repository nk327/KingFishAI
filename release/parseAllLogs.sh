#!/bin/bash
FILES=tournamentlogs/*
for file in $FILES
do
  echo "Parsing $file"
  java GameLogParser "$file"
done
echo "Done parsing all game logs."
