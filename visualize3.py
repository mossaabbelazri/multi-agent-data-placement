import pandas as pd
import matplotlib.pyplot as plt

# Chargement de tes données
df = pd.read_csv(r'C:\Users\Lenovo\Desktop\MASTER\Systemes Multi Agents\Projet\cluster_data.csv')

# 1. Graphique de la Répartition Totale (BAi)
plt.figure(figsize=(8, 5))
df.groupby('Node')['BAi'].sum().plot(kind='bar', color=['blue', 'orange', 'green'])
plt.title('Répartition Totale des Blocs par Worker (Preuve d\'Hétérogénéité)')
plt.ylabel('Nombre total de blocs alloués')
plt.savefig('repartition_blocs.png')


# 2. Graphique de Consommation Disque
plt.figure(figsize=(10, 6))
for node in df['Node'].unique():
    subset = df[df['Node'] == node]
    plt.plot(subset.index, subset['Disk'], label=f'Espace libre {node}')
plt.axhline(y=128, color='r', linestyle='--', label='Seuil de saturation (128Mo)')
plt.title('Consommation Proactive du Disque')
plt.ylabel('Espace libre (Mo)')
plt.legend()
plt.savefig('saturation_disque.png')

plt.show()