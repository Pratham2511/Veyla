#!/usr/bin/env python3
import numpy as np
from PIL import Image

FG = '/home/z/my-project/android/design/veyla-brand/01_VEYLA_MASTER_MARK_TRANSPARENT_2048.png'

img = Image.open(FG).convert('RGBA')
arr = np.array(img)
alpha = arr[:,:,3]
mask = alpha > 128

rows = np.any(mask, axis=1)
cols = np.any(mask, axis=0)
rmin, rmax = np.where(rows)[0][[0, -1]]
cmin, cmax = np.where(cols)[0][[0, -1]]
print(f'Content bbox: ({cmin},{rmin}) to ({cmax},{rmax})')
print(f'Content size: {cmax-cmin+1} x {rmax-rmin+1}')

non_tp = arr[mask]
brightness = non_tp[:,0].astype(float)*0.299 + non_tp[:,1].astype(float)*0.587 + non_tp[:,2].astype(float)*0.114
print(f'Brightness: {brightness.min():.0f} - {brightness.max():.0f}')

threshold = (brightness.min() + brightness.max()) / 2
bright_mask = brightness > threshold
dark_mask = ~bright_mask

for name, cm in [('Bright (ivory)', bright_mask), ('Dark (green/slate)', dark_mask)]:
    indices = np.where(mask)
    plane = np.zeros_like(alpha)
    plane[indices[0][cm], indices[1][cm]] = 255
    rp = np.any(plane > 0, axis=1)
    cp = np.any(plane > 0, axis=0)
    if not np.any(rp):
        print(f'{name}: empty'); continue
    rn, rx = np.where(rp)[0][[0,-1]]
    cn, cx = np.where(cp)[0][[0,-1]]
    print(f'{name}: ({cn},{rn})-({cx},{rx}) size={cx-cn+1}x{rx-rn+1}')
    colors = arr[plane > 0][:,:3]
    print(f'  avg: R={colors[:,0].mean():.0f} G={colors[:,1].mean():.0f} B={colors[:,2].mean():.0f}')

# Horizontal scan at mid
mid_y = (rmin + rmax) // 2
print(f'\nScan y={mid_y}:')
for x in range(cmin, cmax+1, max(1, (cmax-cmin)//20)):
    r,g,b,a = arr[mid_y, x]
    if a > 128:
        print(f'  x={x}: ({r},{g},{b},{a}) br={r*0.299+g*0.587+b*0.114:.0f}')
