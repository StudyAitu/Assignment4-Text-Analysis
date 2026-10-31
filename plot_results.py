"""Plot measured Java benchmark CSV; run after java -cp out Main benchmark."""
import csv
from pathlib import Path
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

rows=list(csv.DictReader(open('results/benchmarks.csv',newline='')))
Path('plots').mkdir(exist_ok=True)

def draw(family,algorithms,scenario,filename,xlabel):
    plt.figure(figsize=(8,5))
    for algo in algorithms:
        subset=sorted((r for r in rows if r['family']==family and r['algorithm']==algo and r['scenario']==scenario),key=lambda r:int(r['size']))
        plt.plot([int(r['size']) for r in subset],[float(r['avg_ms']) for r in subset],marker='o',label=algo)
    plt.xlabel(xlabel);plt.ylabel('Average execution time (ms; 5 measured runs)')
    plt.title(f'{family}: {scenario} workload');plt.grid(alpha=.25);plt.legend();plt.tight_layout()
    plt.savefig('plots/'+filename,dpi=180);plt.close()

draw('DP',['LCS','EditDistance'],'random','dp_times.png','Length of each input string')
draw('Matching',['Naive','KMP','RabinKarp'],'present','matching_present.png','Text length')
draw('Matching',['Naive','KMP','RabinKarp'],'absent','matching_absent.png','Text length')
draw('LCP',['Kasai'],'random','kasai_times.png','String length')
print('Plots generated in plots/')
