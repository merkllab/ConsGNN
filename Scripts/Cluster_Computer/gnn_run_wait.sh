#!/bin/bash

# this script generates GNNs and has to be executed on EFI computers

LIST="list_gnn"
TEMP="tempG"
typeset -i i=0
typeset -i j=0

i=$(wc -l ${LIST} | cut -d" " -f1)
while [ ${i} -gt 0 ]; do
  read IPR <${LIST}
  WAIT="true"
  echo "${IPR}" >>out_listGNN
  echo "${IPR}"
  i=`wc -l ${LIST}| cut -d" " -f1`
  (( i = $i - 1  ))
  tail -n${i} ${LIST} > ${TEMP}
  mv ${TEMP} ${LIST}
  mkdir ${IPR}gnn
  cp ${IPR}FIL.xgmml ${IPR}gnn
  cd ${IPR}gnn
  cmd="submit_gnn.pl -ssnin ${IPR}FIL.xgmml -queue efi-mem -gnn-only -nb-size 10 -cooc 20"
  jobs=( $($cmd | grep "^ " ) )
  echo ${jobs[*]} >>../out_listGNN
  numjobs=`echo ${#jobs[*]}`
  (( numjobs = ${numjobs}-1 ))
  while [ "$WAIT" = "true" ]; do
    sleep 20
    date > ../dateGNN
    WAIT="false"
    for j in `seq 0 ${numjobs}` ; do
    	runs=$(squeue | grep "${jobs[$j]}" | wc -l)
	if [ $runs -gt 0 ]; then
          WAIT="true"
	fi
    done
  done
  cd ..
done
