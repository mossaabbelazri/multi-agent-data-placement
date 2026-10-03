#!/bin/bash
echo "--- ARRÊT DU CLUSTER HÉTÉROGÈNE ---"

# 1. Arrêter les agents JADE sur toutes les machines
echo "[1/2] Arrêt des agents JADE (Master + Workers)..."
pkill -9 java
ssh Worker-1 "pkill -9 java"
ssh Worker-2 "pkill -9 java"
ssh Worker-3 "pkill -9 java"

# 2. Arrêter les services Hadoop
echo "[2/2] Arrêt de Hadoop HDFS et YARN..."
stop-yarn.sh
stop-dfs.sh

echo "-------------------------------------------"
echo "Tout est arrêté proprement."
