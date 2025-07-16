#!/bin/bash

EPOCHS=3

DATA_DIR="${PWD}/data"

MODELS=(
    # "bert_en_uncased_L-12_H-768_A-12"
    "small_bert/bert_en_uncased_L-2_H-128_A-2" # tiny
    # "small_bert/bert_en_uncased_L-2_H-256_A-4"
    # "small_bert/bert_en_uncased_L-2_H-512_A-8"
    # "small_bert/bert_en_uncased_L-2_H-768_A-12"
    # "small_bert/bert_en_uncased_L-4_H-128_A-2"
    "small_bert/bert_en_uncased_L-4_H-256_A-4" # mini
    "small_bert/bert_en_uncased_L-4_H-512_A-8" # small
    # "small_bert/bert_en_uncased_L-4_H-768_A-12"
    # "small_bert/bert_en_uncased_L-6_H-128_A-2"
    # "small_bert/bert_en_uncased_L-6_H-256_A-4"
    # "small_bert/bert_en_uncased_L-6_H-512_A-8"
    # "small_bert/bert_en_uncased_L-6_H-768_A-12"
    # "small_bert/bert_en_uncased_L-8_H-128_A-2"
    # "small_bert/bert_en_uncased_L-8_H-256_A-4"
    "small_bert/bert_en_uncased_L-8_H-512_A-8" # medium
    # "small_bert/bert_en_uncased_L-8_H-768_A-12"
    # "small_bert/bert_en_uncased_L-10_H-128_A-2"
    # "small_bert/bert_en_uncased_L-10_H-256_A-4"
    # "small_bert/bert_en_uncased_L-10_H-512_A-8"
    # "small_bert/bert_en_uncased_L-10_H-768_A-12"
    # "small_bert/bert_en_uncased_L-12_H-128_A-2"
    # "small_bert/bert_en_uncased_L-12_H-256_A-4"
    # "small_bert/bert_en_uncased_L-12_H-512_A-8"
    "small_bert/bert_en_uncased_L-12_H-768_A-12" # base
)

GLUE_TASKS=(
    "wnli"
    "cola"
    "sst2"
    "mrpc"
    "qqp"
    "stsb"
    "mnli"
    # "mnli_mismatched"
    # "mnli_matched"
    "qnli"
    "rte"
    "ax"
)

for model in ${MODELS[@]}; do
    for task in ${GLUE_TASKS[@]}; do
        for i in `seq 0 1 4`; do
            output_path="${DATA_DIR}/${model//\//@}/${task}/instance-${i}/report-${i}.csv"
            python3 ${PWD}/bert_glue_finetune.py \
                --model "${model}" \
                --task "${task}" \
                --epochs "${EPOCHS}" \
                --output_path "${output_path}"
        done
    done
done
