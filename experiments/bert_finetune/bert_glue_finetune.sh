#!/bin/bash

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

DATA_DIR="${PWD}/glue-test-data"
EPOCHS=1
BATCH_SIZE=32
MODEL="small_bert/bert_en_uncased_L-2_H-128_A-2"
GLUE_TASK="cola"

output_path="${DATA_DIR}/${MODEL//\//@}/${GLUE_TASK}/report.csv"
python3 ${PWD}/bert_glue_finetune.py \
    --model "${MODEL}" \
    --task "${GLUE_TASK}" \
    --epochs "${EPOCHS}" \
    --batch_size "${BATCH_SIZE}" \
    --output_path "${output_path}"
