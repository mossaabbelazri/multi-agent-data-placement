import pandas as pd
import matplotlib.pyplot as plt

# Chargement des données générées par le MasterAgent
# Utilisation du chemin complet vers votre dossier de projet
df = pd.read_csv(r'C:\Users\Lenovo\Desktop\MASTER\Systemes Multi Agents\Projet\cluster_data.csv')

# 1. Graphique de la Capacité Ci (Évolution de la performance)
plt.figure(figsize=(12, 6))
for node in df['Node'].unique():
    data = df[df['Node'] == node]
    plt.plot(data['Timestamp'], data['Ci'], marker='o', linestyle='--', label=f'Score {node}')

plt.title('Évolution de la Capacité (Ci) par Nœud')
plt.xlabel('Timestamp (s)')
plt.ylabel('Score Ci')
plt.legend()
plt.grid(True, alpha=0.3)
plt.savefig('1_capacite_nodes.png')
plt.show()

# 2. Graphique de la Consommation Disque (Espace libre réel)
# Ce graphique montre l'impact direct des commandes SSH fallocate
plt.figure(figsize=(12, 6))
for node in df['Node'].unique():
    data = df[df['Node'] == node]
    plt.plot(data['Timestamp'], data['Disk'], marker='s', label=f'Espace libre {node}')

# Ligne de sécurité critique à 128 Mo
plt.axhline(y=128, color='r', linestyle=':', label='Seuil de sécurité (128Mo)')

plt.title('Consommation Progressive du Disque (Espace Libre en Mo)')
plt.xlabel('Timestamp (s)')
plt.ylabel('Espace Libre (Mo)')
plt.legend()
plt.grid(True, alpha=0.3)
plt.savefig('2_consommation_disque.png')
plt.show()

# 3. Graphique de répartition des blocs (Allocation Dynamique)
pivot_df = df.pivot(index='Timestamp', columns='Node', values='BAi')
pivot_df.plot(kind='bar', stacked=True, figsize=(12, 6), colormap='viridis')
plt.title('Répartition dynamique des Blocs (Impact de la saturation)')
plt.xlabel('Cycle de temps')
plt.ylabel('Nombre de blocs (Total 100 Mo)')
plt.legend(title="Workers")
plt.grid(axis='y', linestyle='--', alpha=0.7)
plt.savefig('3_repartition_blocs.png')
plt.show()