import scipy.stats as sps

a = [1.5, 3.5, 2.0, 1.0, -1.0]
b = [2.5, 0.0, 0.5, 2.5, -0.5]

x = [-1.0, 1.0, 1.5, 2.0, 3.5]
y = [-0.5, 2.5, 2.5, 0.5, 0.0]

print sps.kendalltau(a, b)
print sps.kendalltau(x, y)
