#!/bin/bash
percent=
if [ -e /home/runner/work/AuScope-Portal-API/AuScope-Portal-API/target/site/jacoco/jacoco.csv ]
then
    percent=`awk -F, '/AuScope-Portal-API/{sumIM+= $4; sumIC+=$5} END {print sumIC/(sumIM+sumIC)*100}' /home/runner/work/AuScope-Portal-API/AuScope-Portal-API/target/site/jacoco/jacoco.csv`
else
    percent="coverage file [jacoco.csv] not found"
fi

echo $percent
