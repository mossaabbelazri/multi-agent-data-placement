import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# --- CONFIGURATION ---
# Si le fichier est dans le même dossier, laissez 'cluster_data.csv'
# Sinon, utilisez r'C:\Users\Lenovo\Desktop\MASTER\Systemes Multi Agents\Projet\cluster_data.csv'
df = pd.read_csv(r'C:\Users\Lenovo\Desktop\MASTER\Systemes Multi Agents\Projet\cluster_data.csv')

# 1. Évolution de la Capacité Ci (Intelligence des agents)
plt.figure(figsize=(10, 6))
for node in df['Node'].unique():
    data = df[df['Node'] == node]
    plt.plot(data['Timestamp'], data['Ci'], marker='o', label=f'Score {node}')
plt.title('Évolution de la Capacité (Ci) par Nœud')
plt.xlabel('Timestamp (Secondes)')
plt.ylabel('Score de Capacité Ci')
plt.legend()
plt.grid(True, alpha=0.3)
plt.savefig('1_capacite_nodes.png')

# 2. Consommation Disque (Action physique SSH)
plt.figure(figsize=(10, 6))
for node in df['Node'].unique():
    data = df[df['Node'] == node]
    plt.plot(data['Timestamp'], data['Disk'], marker='s', label=f'Espace libre {node}')
plt.axhline(y=128, color='r', linestyle=':', label='Seuil de sécurité (128Mo)')
plt.title('Consommation du Disque et Protection (Seuil 128Mo)')
plt.xlabel('Timestamp (Secondes)')
plt.ylabel('Espace Libre (Mo)')
plt.legend()
plt.grid(True, alpha=0.3)
plt.savefig('2_consommation_disque.png')

# 3. Répartition Dynamique (Stacked Bar)
pivot_df = df.pivot(index='Timestamp', columns='Node', values='BAi')
pivot_df.plot(kind='bar', stacked=True, figsize=(10, 6), colormap='viridis')
plt.title('Répartition des Blocs Hadoop par Cycle')
plt.xlabel('Cycles de temps')
plt.ylabel('Nombre de blocs (Allocation)')
plt.legend(title="Workers")
plt.savefig('3_repartition_blocs.png')

# 4. NOUVEAU : Comparaison JADE vs Hadoop Standard
# On calcule le total de blocs que JADE a mis sur chaque noeud
jade_totals = df.groupby('Node')['BAi'].sum()
nodes = jade_totals.index.tolist()

# Hadoop Standard aurait divisé les blocs équitablement (Round Robin)
total_blocks_placed = df['BAi'].sum()
standard_val = total_blocks_placed / len(nodes)
standard_totals = [standard_val] * len(nodes)

x = np.arange(len(nodes))
width = 0.35

fig, ax = plt.subplots(figsize=(10, 6))
rects1 = ax.bar(x - width/2, jade_totals, width, label='Placement JADE (Agent)', color='skyblue')
rects2 = ax.bar(x + width/2, standard_totals, width, label='Hadoop Standard (Égal)', color='orange')

ax.set_ylabel('Nombre Total de Blocs')
ax.set_title('Bilan Global : JADE vs Hadoop Standard')
ax.set_xticks(x)
ax.set_xticklabels(nodes)
ax.legend()
ax.bar_label(rects1, padding=3)
ax.bar_label(rects2, fmt='%.1f', padding=3)

plt.grid(axis='y', linestyle='--', alpha=0.5)
plt.tight_layout()
plt.savefig('4_comparaison_jade_hadoop.png')

print("Les 4 graphiques ont été générés.")
plt.show()