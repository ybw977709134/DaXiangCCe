package com.daxiangce123.android.ui.activities.base;

import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project Cliq
 * @time Mar 19, 2014
 * @author ram
 */
public class BaseFragmentActivity extends FragmentActivity {

	public final static String TAG = "BaseFragmentActivity";
	/**
	 * current all fragment which can be shown by back
	 */
	private LinkedList<BaseFragment> currentList;
	/**
	 * all fragment which cant be show by back
	 */
	private LinkedList<BaseFragment> cachedList;
	private BaseFragment curFragment;
	private boolean DEBUG = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initList();
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		// overridePendingTransition(R.anim.right_in, R.anim.scale_to_small);
		super.onCreate(savedInstanceState);
	}

	private void initList() {
		if (currentList == null) {
			currentList = new LinkedList<BaseFragment>();
		}
		if (cachedList == null) {
			cachedList = new LinkedList<BaseFragment>();
		}
	}

	protected boolean onBack(JSONObject jo) {
		if (curFragment == null) {
			return false;
		}
		if (curFragment.onBackPressed()) {
			return true;
		}
		return showPrevious(jo);
	}

	protected BaseFragment getCurrentFragment() {
		if (currentList != null && currentList.size() != 0) {
			return currentList.getLast();
		}
		return null;
	}

	protected boolean showPrevious(JSONObject jo) {
		if (curFragment == null) {
			return false;
		}
		int length = currentList.size();
		if (length <= 1) {
			return false;
		}
		try {
			FragmentTransaction transaction = getTransaction();
			// transaction.setCustomAnimations(R.anim.scale_from_small_to_normal,
			// R.anim.right_out);
			BaseFragment previousFragment = currentList.removeLast();
			transaction.remove(previousFragment);

			curFragment = currentList.getLast();
			curFragment.setData(jo);
			hideOtherFragment(curFragment, transaction);
			transaction.commitAllowingStateLoss();
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public void finish() {
		// overridePendingTransition(R.anim.scale_from_small_to_normal,
		// R.anim.right_out);
		super.finish();
	}

	public void clearFragments() {
		// //removeUseLess();
		FragmentTransaction transaction = getTransaction();
		if (currentList != null) {
			for (BaseFragment frag : currentList) {
				transaction.remove(frag);
			}
			currentList.clear();
		}
		transaction.commitAllowingStateLoss();

		if (cachedList != null) {
			cachedList.clear();
		}
	}

	/**
	 * This will directly back to previous fragment without care about
	 * {@link BaseFragment#onBackPressed()}. If there is just on fragment.The
	 * attached activity will be {@link #finish()}
	 * 
	 * @time May 4, 2014
	 * 
	 * @param jo
	 */
	public void back(JSONObject jo) {
		if (!showPrevious(jo)) {
			finish();
		}
	}

	/**
	 * @see BaseFragmentActivity#back(JSONObject)
	 */
	@Override
	public void onBackPressed() {
		if (onBack(null)) {
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		if (DEBUG) {
			LogUtil.d(TAG, getClass().getSimpleName() + " onDestroy()");
		}
		onFinish();
		super.onDestroy();
	}

	public BaseFragment getCurrent() {
		return curFragment;
	}

	/**
	 * just show this fragment, but previous fragment in this activity will not
	 * be removed(just be hidden)
	 * 
	 * @time Mar 19, 2014
	 * 
	 * @param fragment
	 * @return
	 */
	public boolean showOnly(BaseFragment fragment) {
		if (fragment == null) {
			return false;
		}
		try {
			curFragment = fragment;
			FragmentTransaction transaction = getTransaction();
			int index = -1;

			/* if is the time call this, remove all fragments to cachedList */
			if (Utils.isEmpty(cachedList)) {
				for (BaseFragment baseFragment : currentList) {
					if (baseFragment == null) {
						continue;
					}
					if (fragment == baseFragment) {
						index = 0;
						transaction.show(baseFragment);
					} else {
						transaction.hide(baseFragment);
					}
					cachedList.add(baseFragment);
				}
			}

			for (int i = 0; i < cachedList.size(); i++) {
				BaseFragment baseFragment = cachedList.get(i);
				if (fragment == baseFragment) {
					index = i;
					transaction.show(fragment);
				} else {
					transaction.hide(baseFragment);
				}
			}

			try {
				if (index >= 0) {
					cachedList.remove(index);
				} else {
					transaction.add(R.id.fragment_content, fragment);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			cachedList.add(fragment);
			currentList.clear();
			currentList.add(fragment);
			transaction.commitAllowingStateLoss();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean removeAndShow(BaseFragment previous, BaseFragment current) {
		if (current == null) {
			return false;
		}
		if (previous == null) {
			return showFragment(current);
		}
		try {
			FragmentTransaction transaction = getTransaction();
			removeFragment(previous, transaction);
			curFragment = current;
			int index = hideOtherFragment(current, transaction);
			if (index < 0) {
				transaction.add(R.id.fragment_content, current);
			} else {
				// exists before. so make it at the end of the list
				currentList.remove(index);
			}
			currentList.add(current);
			transaction.commitAllowingStateLoss();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean showFragment(BaseFragment fragment) {
		if (fragment == null) {
			return false;
		}
		curFragment = fragment;
		FragmentTransaction transaction = getTransaction();
		// transaction.setCustomAnimations(R.anim.right_in,
		// R.anim.scale_to_small);
		int index = hideOtherFragment(fragment, transaction);
		if (index < 0) {
			transaction.add(R.id.fragment_content, fragment);
		} else {
			// exists before. so make it at the last of the list
			currentList.remove(index);
		}
		currentList.add(fragment);
		transaction.commitAllowingStateLoss();
		return true;
	}

	private void onFinish() {
		if (DEBUG) {
			LogUtil.d(TAG, getClass().getSimpleName() + " onFinish()");
		}
		clearFragments();
		curFragment = null;
		cachedList = null;
		currentList = null;
		System.gc();
	}

	@SuppressLint("CommitTransaction")
	private FragmentTransaction getTransaction() {
		// //removeUseLess();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		return transaction;
	}

	// private void removeUseLess() {
	// FragmentManager fragmentManager = getSupportFragmentManager();
	// List<Fragment> fragments = fragmentManager.getFragments();
	// if (Utils.sizeOf(fragments) > 0) {
	// if (Utils.sizeOf(cachedList) + Utils.sizeOf(currentList) <= 0) {
	// return;
	// }
	// List<Fragment> toBeDel = new ArrayList<Fragment>();
	// for (Fragment fragment : fragments) {
	// if (cachedList != null && cachedList.contains(fragment)) {
	// continue;
	// }
	// if (currentList != null && currentList.contains(fragment)) {
	// continue;
	// }
	// toBeDel.add(fragment);
	// }
	// if (Utils.sizeOf(toBeDel) > 0) {
	// LogUtil.d(TAG, "toBeDel size=" + toBeDel.size());
	// try {
	// FragmentTransaction transaction = fragmentManager
	// .beginTransaction();
	// for (Fragment fragment : toBeDel) {
	// transaction.remove(fragment);
	// }
	// transaction.commitAllowingStateLoss();
	// // fragments.removeAll(toBeDel);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	private void removeFragment(BaseFragment fragment, FragmentTransaction transaction) {
		if (fragment == null || transaction == null) {
			return;
		}
		transaction.remove(fragment);
		if (!Utils.isEmpty(currentList)) {
			currentList.remove(fragment);
		}
	}

	/**
	 * @warning without commit
	 * 
	 * @time Mar 19, 2014
	 * 
	 * @param fragment
	 * @param transaction
	 * @return
	 */
	private int hideOtherFragment(BaseFragment fragment, FragmentTransaction transaction) {
		if (fragment == null || transaction == null) {
			return -1;
		}
		if (Utils.isEmpty(currentList)) {
			return -1;
		}
		int index = -1;
		for (int i = 0; i < currentList.size(); i++) {
			BaseFragment baseFragment = currentList.get(i);
			if (baseFragment == fragment) {
				index = i;
				if (baseFragment.isHidden()) {
					transaction.show(baseFragment);
				}
			} else {
				// if (baseFragment.isVisible())
				transaction.hide(baseFragment);
			}
		}
		return index;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// clearFragments();
		// super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (curFragment != null) {
			curFragment.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
