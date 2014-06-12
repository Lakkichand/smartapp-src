package com.jiubang.ggheart.apps.desks.appfunc.model;

//import android.database.DataSetObserver;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;

public interface GAdapter {
	/**
	 * Register an observer that is called when changes happen to the data used
	 * by this adapter.
	 * 
	 * @param observer
	 *            the object that gets notified when the data set changes.
	 */
	void registerDataSetObserver(DataSetObserver observer);

	/**
	 * Unregister an observer that has previously been registered with this
	 * adapter via {@link #registerDataSetObserver}.
	 * 
	 * @param observer
	 *            the object to unregister.
	 */
	void unregisterDataSetObserver(DataSetObserver observer);

	/**
	 * How many items are in the data set represented by this Adapter.
	 * 
	 * @return Count of items.
	 */
	int getCount();

	/**
	 * Get the data item associated with the specified position in the data set.
	 * 
	 * @param position
	 *            Position of the item whose data we want within the adapter's
	 *            data set.
	 * @return The data at the specified position.
	 */
	Object getItem(int position);

	/**
	 * Get the row id associated with the specified position in the list.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set whose
	 *            row id we want.
	 * @return The id of the item at the specified position.
	 */
	long getItemId(int position);

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set. You can either create a View manually or inflate it from an XML
	 * layout file. When the View is inflated, the parent View (GridView,
	 * ListView...) will apply default layout parameters unless you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	XComponent getComponent(int position, int x, int y, int width, int height,
			XComponent convertView, XPanel parent);

	/**
	 * @return true if this adapter doesn't contain any data. This is used to
	 *         determine whether the empty view should be displayed. A typical
	 *         implementation will return getCount() == 0 but since getCount()
	 *         includes the headers and footers, specialized adapters might want
	 *         a different behavior.
	 */
	boolean isEmpty();

	/**
	 * Returns true if the item at the specified position is not a separator. (A
	 * separator is a non-selectable, non-clickable item).
	 * 
	 * @param position
	 *            Index of the item
	 * @return True if the item is not a separator
	 */
	boolean isEnabled(int position);

	/**
	 * Are all items in this ListAdapter enabled? If yes it means all items are
	 * selectable and clickable.
	 * 
	 * @return True if all items are enabled
	 */
	public boolean areAllItemsEnabled();
}
