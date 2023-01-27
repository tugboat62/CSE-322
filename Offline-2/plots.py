import numpy as np
from scipy.interpolate import make_interp_spline
import matplotlib.pyplot as plt

file = open('data.txt', 'r')
grids = [250, 375, 500, 625, 750, 875, 1000, 1125, 1250]
nodes = [20, 30, 40, 50, 60, 70, 80, 90, 100]
flows = [10, 15, 20, 25, 30, 35, 40, 45, 50]
gridsTp = []
gridsAd = []
gridsDr = []
gridsDrat = []
nodesTp = []
nodesAd = []
nodesDr = []
nodesDrat = []
flowsTp = []
flowsAd = []
flowsDr = []
flowsDrat = []
while True:
    line = file.readline()
    if not line:
        break
    tokens = line.split()
    mesType = tokens[0]
    size = int(tokens[1])
    throughput = float(tokens[2])
    avgDelay = float(tokens[3])
    delRate = float(tokens[4])
    dropRatio = float(tokens[5])

    if mesType == 'grid':
        gridsTp.append(throughput)
        gridsAd.append(avgDelay)
        gridsDr.append(delRate)
        gridsDrat.append(dropRatio)

    elif mesType == 'nodes':
        nodesTp.append(throughput)
        nodesAd.append(avgDelay)
        nodesDr.append(delRate)
        nodesDrat.append(dropRatio)

    elif mesType == 'flows':
        flowsTp.append(throughput)
        flowsAd.append(avgDelay)
        flowsDr.append(delRate)
        flowsDrat.append(dropRatio)

file.close()

gridsList = [gridsTp, gridsAd, gridsDr, gridsDrat]
nodesList = [nodesTp, nodesAd, nodesDr, nodesDrat]
flowsList = [flowsTp, flowsAd, flowsDr, flowsDrat]

for i in range(len(gridsList)):
    x = np.array(grids)
    y = np.array(gridsList[i])
    X_Y_Spline = make_interp_spline(x, y)
    X_ = np.linspace(x.min(), x.max(), 500)
    Y_ = X_Y_Spline(X_)
    plt.plot(X_, Y_)
    plt.xlabel('grid size')
    if i == 0:
        plt.ylabel('Throughput')
    elif i == 1:
        plt.ylabel('Average Delay')
    elif i == 2:
        plt.ylabel('Delivery Rate')
    elif i == 3:
        plt.ylabel('Drop Ratio')
    plt.show()

for i in range(len(nodesList)):
    x = np.array(nodes)
    y = np.array(nodesList[i])
    X_Y_Spline = make_interp_spline(x, y)
    X_ = np.linspace(x.min(), x.max(), 500)
    Y_ = X_Y_Spline(X_)
    plt.plot(X_, Y_)
    plt.xlabel('nodes')
    if i == 0:
        plt.ylabel('Throughput')
    elif i == 1:
        plt.ylabel('Average Delay')
    elif i == 2:
        plt.ylabel('Delivery Rate')
    elif i == 3:
        plt.ylabel('Drop Ratio')
    plt.show()

for i in range(len(flowsList)):
    x = np.array(flows)
    y = np.array(flowsList[i])
    X_Y_Spline = make_interp_spline(x, y)
    X_ = np.linspace(x.min(), x.max(), 500)
    Y_ = X_Y_Spline(X_)
    plt.plot(X_, Y_)
    plt.xlabel('flows')
    if i == 0:
        plt.ylabel('Throughput')
    elif i == 1:
        plt.ylabel('Average Delay')
    elif i == 2:
        plt.ylabel('Delivery Rate')
    elif i == 3:
        plt.ylabel('Drop Ratio')
    plt.show()
