package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;

import com.daxiangce123.android.data.CommentEntity;

public class DeleteCommentPopup extends OptionDialog implements
		OnItemClickListener {
	private CommentEntity commentEntity;

	public DeleteCommentPopup(Context context) {
		super(context);
		initDialog();
	}

	private void initDialog() {
		setCancelable(true);
		setCanceledOnTouchOutside(true);
	}

	public void setCommentData(CommentEntity entity) {
		this.commentEntity = entity;
	}

	@Override
	protected Object getOptionObj() {
		return commentEntity;
	}
}