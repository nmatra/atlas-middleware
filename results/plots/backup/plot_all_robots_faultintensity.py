#!/usr/bin/python
import csv
import sys
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.collections import PatchCollection
from matplotlib.patches import Rectangle
from numpy import genfromtxt

# Load the result files
# filename = "/home/jharbin/academic/atlas/atlas-middleware/results/12_05_2020_2353/SPEEDFAULT-ELLA_goalDiscovery.res"
filename = sys.argv[1]
my_data = genfromtxt(filename, delimiter=',')

fault_nums = my_data[:,0]
[index,counts] = np.unique(fault_nums, return_counts=True)

# Get the start end time range for the particular experiment...
starts = my_data[:,2]
ends = my_data[:,3]
lengths = ends - starts

max_len = 1000

# Need to create a rectange for each of the fault definitions at starts and ends

fig,ax  = plt.subplots(1)

ypos = 0
for i in range(0,len(index)):
    v_for_cell = counts[i] / 6
    flines = np.where(fault_nums==i)
    fline = flines[0][0]
    end_this_rect = starts[fline] + lengths[fline]
    # Need to increment ypos when the end of the line is reached
    rect = Rectangle((starts[fline],ypos),lengths[fline],1, color=(1,v_for_cell,1), edgecolor="Black")
    if (end_this_rect >= max_len):
        ypos+=1;
    ax.add_patch(rect)
    # How to set the colour

plt.ylim(0,30);
plt.xlim(0,1000);
plt.xlabel("Mission time range");
plt.title("Impact of faults upon missed detections by UUV " + sys.argv[2] + "\n(intensity of colour is more missed detections)");
plt.savefig("coverage_singlefault_" + sys.argv[2]);
