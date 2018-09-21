package com.daxiangce123.android.data;

import java.util.LinkedList;
import java.util.List;

import com.daxiangce123.android.util.Utils;

public class FileComments {
	private int limit;
	private boolean hasMore;
	private LinkedList<CommentEntity> comments;

	public FileComments() {

	}

	public FileComments(int limit, boolean hasMore,
			LinkedList<CommentEntity> comments) {
		this.limit = limit;
		this.hasMore = hasMore;
		this.comments = comments;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public void setComments(LinkedList<CommentEntity> comments) {
		this.comments = comments;
	}

	public LinkedList<CommentEntity> getcomments() {
		return comments;
	}

	public boolean add(FileComments comments) {
		if (comments == null) {
			return false;
		}
		this.limit = comments.getLimit();
		this.hasMore = comments.hasMore;
		add(comments.getcomments());
		return true;
	}

	public void add(List<CommentEntity> entities) {
		if (Utils.isEmpty(entities)) {
			return;
		}
		for (int i = 0; i < entities.size(); i++) {
			add(entities.get(i));
		}
	}

	public boolean add(CommentEntity commentEntity) {
		if (commentEntity == null) {
			return false;
		}
		try {
			if (comments == null) {
				comments = new LinkedList<CommentEntity>();
			} else {
				for (int i = 0; i < comments.size(); i++) {
					CommentEntity entity = comments.get(i);
					if (commentEntity.getId().equals(entity.getId())) {
						entity.setCreateDate(commentEntity.getCreateDate());
						entity.setId(commentEntity.getId());
						entity.setMsg(commentEntity.getMsg());
						entity.setObjId(commentEntity.getObjId());
						entity.setObjType(commentEntity.getObjType());
						entity.setReplyToUser(commentEntity.getReplyToUser());
						entity.setReplyToUserName(commentEntity
								.getReplayToUserName());
						entity.setUserId(commentEntity.getUserId());
						entity.setUserName(commentEntity.getUserName());
						entity = null;
						return false;
					}
				}
			}
			return comments.add(commentEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
