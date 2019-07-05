#!/bin/bash

# this script generates SSNs and has to be executed on EFI computers

LIST="list"
TEMP="tempfile"
OUT="logssn"
STATUS="status"
DATE="date"
typeset -i i=0
typeset -i j=0

function exit_file(){
  if [ -f "exit" ] ; then
        echo "exit caused by file" >>${OUT}
        exit 0
  fi
}

i=$(wc -l ${LIST} | cut -d" " -f1)
NP=24
while [ ${i} -gt 0 ]; do
  read IPR <${LIST}
  WAIT="true"
  echo "${IPR}" >>${OUT}
  echo "${IPR}"
  i=`wc -l ${LIST}| cut -d" " -f1`
  (( i = $i - 1  ))
  tail -n${i} ${LIST} > ${TEMP}
  mv ${TEMP} ${LIST}

  CNT=`squeue --format '%C %N' -p efi | grep compute | cut -f1 -d" " | paste -s -d+ | bc`
  if [ ! -n $CNT ]; then
   while [ $CNT -gt "180" ]; do
    sleep 20
    echo "${CNT}: waiting" >${STATUS}
    CNT=`squeue --format '%C %N' -p efi | grep compute | cut -f1 -d" " | paste -s -d+ | bc`
    exit_file
   done
  fi
  echo "running" >${STATUS}
  date >> ${STATUS}

  cmd="generatedata.pl -queue efi -memqueue efi-mem -tmp ${IPR} -ipro ${IPR} -np ${NP}"
  jobs=( $($cmd | grep "^ " ) )
  echo ${jobs[*]} >>${OUT}
  numjobs=`echo ${#jobs[*]}`
  (( numjobs = ${numjobs}-1 ))
  while [ "$WAIT" = "true" ]; do
    sleep 20
    date > ${DATE}
    WAIT="false"
    for j in `seq 0 ${numjobs}` ; do
    	runs=$(squeue | grep "${jobs[$j]}" | wc -l)
	if [ $runs -gt 0 ]; then
          WAIT="true"
	fi
    done
  done
  echo "analyze ${IPR} ..." >>${OUT}
  analyzedata.pl -queue efi -tmp ${IPR} -title ${IPR} -filter eval -minval 5
  exit_file
done
