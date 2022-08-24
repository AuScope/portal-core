#!/bin/bash
percent=
if [ -e /home/runner/work/portal-core/portal-core/target/site/jacoco/jacoco.csv ]
then
    percent=`awk -F, '/Portal-Core/{sumIM+= $4; sumIC+=$5} END {print sumIC/(sumIM+sumIC)*100}' /home/runner/work/portal-core/portal-core/target/site/jacoco/jacoco.csv`
else
    percent="coverage file [jacoco.csv] not found"
fi

echo $percent
