package com.gau.go.launcherex.theme.cover;

/**
 * 
 * @author chenqiang
 *
 */
public interface OnCreateAtomListener {
    public static final int TYPE_ATOM_TOUCH = 0;
    public static final int TYPE_ATOM_SPIRIT = 1;
	public boolean onCreateAtom(int type, int startX, int startY);
	
}
