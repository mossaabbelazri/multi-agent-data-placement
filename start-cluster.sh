#!/bin/bash

# Définition du chemin vers le JAR de JADE
JADE_PATH="/home/hadoop/jade/lib/jade.jar:."

echo "--- DÉMARRAGE DU CLUSTER HÉTÉROGÈNE (HADOOP + JADE) ---"

# 1. Démarrage Hadoop
echo "[1/3] Lancement de Hadoop..."
start-dfs.sh
start-yarn.sh
sleep 8

# 2. Lancement du MasterAgent avec l'interface graphique (-gui)
echo "[2/3] Lancement de MasterAgent avec interface RMA..."
# L'option -gui déclenche l'affichage via le serveur X11 de MobaXterm
java -cp $JADE_PATH jade.Boot -gui -host Master -agents "MasterAgent:MasterAgent" &
sleep 5

# 3. Lancement des SlaveAgents sur les Workers (Pas de changement ici)
echo "[3/3] Déploiement des SlaveAgents sur les Workers..."
ssh Worker-1 "java -cp $JADE_PATH jade.Boot -host Master -container -agents W1:SlaveAgent" &
ssh Worker-2 "java -cp $JADE_PATH jade.Boot -host Master -container -agents W2:SlaveAgent" &
ssh Worker-3 "java -cp $JADE_PATH jade.Boot -host Master -container -agents W3:SlaveAgent" &

echo "--------------------------------------------------------"
echo "Cluster opérationnel ! L'interface JADE va s'ouvrir..."
echo "--------------------------------------------------------"

wait