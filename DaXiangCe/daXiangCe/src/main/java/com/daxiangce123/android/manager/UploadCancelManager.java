package com.daxiangce123.android.manager;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.daxiangce123.android.App;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.FileUploadManager.CreateTask;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

public class UploadCancelManager {

	private static final String TAG = "UploadCancelManager";

	private static final boolean DEBUG = true;

	private static UploadCancelManager mInstance = null;

	private Vector<FileEntity> cancleLinkedList = new Vector<FileEntity>();

	public static UploadCancelManager sharedInstance() {
		if (mInstance == null) {
			mInstance = new UploadCancelManager();
		}
		return mInstance;
	}

	private UploadCancelManager() {
		StringBuffer log = new StringBuffer();
		log.append("\n");
		log.append("                   _ooOoo_\n");
		log.append("                  o8888888o\n");
		log.append("                  88\" . \"88\n");
		log.append("                  (| -_- |)\n");
		log.append("                  O\\  =  /O\n");
		log.append("               ____/`---'\\____\n");
		log.append("             .'  \\\\|     |//  `.\n");
		log.append("            /  \\\\|||  :  |||//  \\ \n");
		log.append("           /  _||||| -:- |||||-  \\ \n");
		log.append("           |   | \\\\\\  -  /// |   |\n");
		log.append("           | \\_|  ''\\---/''  |   |\n");
		log.append("           \\  .-\\__  `-`  ___/-. /\n");
		log.append("         ___`. .'  /--.--\\  `. . __\n");
		log.append("      .\"\" '<  `.___\\_<|>_/___.'  >'\"\".\n");
		log.append("     | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |\n");
		log.append("     \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /\n");
		log.append("======`-.____`-.___\\_____/___.-`____.-'======\n");
		log.append("                   `=---='\n");
		log.append("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
		log.append("\t\t佛祖保佑       永无BUG\n");
		if (DEBUG) {
			LogUtil.v(TAG, log.toString());
		}
	}

	public void cancelUpload(FileEntity file) {
		file.createFakeId();
		if (DEBUG) {
			LogUtil.v(TAG, "file.createFakeId(); " + file.getFakeId());
		}
		cancleLinkedList.add(file);
		deleteFileFromDb(file);
		cancleFileCreate(file);
		cancelConnection(file);
		deleteFileFromServer(file);
	}

	private void cancelConnection(FileEntity file) {
		HttpUploadManager.instance().cancelUploadConnect(file.getFakeId());
		RequestManager.sharedInstance().cancelUploadConnect(file.getFakeId());
	}

	private void cancleFileCreate(FileEntity file) {
		Vector<CreateTask> createTasks = FileUploadManager.instance().getUploadList();
		Hashtable<String, Integer> batchs = FileUploadManager.instance().getUploadTask();
		if (createTasks == null || batchs == null) {
			return;
		}
		for (Iterator<CreateTask> it = createTasks.iterator(); it.hasNext();) {
			CreateTask createTask = it.next();
			if (checkTask(createTask)) {
				if (DEBUG) {
					LogUtil.v(TAG, "cancleFileCreate " + file.getFakeId());
				}
				it.remove();
				synchronized (batchs) {
					int count = batchs.get(createTask.batchId);
					count--;
					if (count <= 0) {
						batchs.remove(createTask.batchId);
					} else {
						batchs.put(createTask.batchId, count);
					}
				}

			}

		}
	}

	/**
	 * delete file form Db
	 * 
	 * @param file
	 */
	private void deleteFileFromDb(final FileEntity file) {
		TaskRuntime.instance().run(new Runnable() {
			@Override
			public void run() {
				DBHelper dbHelper = App.getDBHelper();
				try {
					String fileId = file.getId();
					if (Utils.isEmpty(fileId) || dbHelper == null) {
						return;
					}
					boolean b_Id = dbHelper.delete(FileEntity.EMPTY, fileId);
					boolean b_fId = dbHelper.delete(FileEntity.EMPTY, file.getFakeId());
					if (DEBUG) {
						LogUtil.v(TAG, "file.deleteFileFromDb(); b_Id:" + b_Id + " b_fId:" + b_fId);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dbHelper.close();
				}

			}
		});
	}

	/**
	 * delete exist file from server use in EventService::onFileCreate()
	 * 
	 * @param file
	 */
	public void deleteFileFromServer(FileEntity file) {
		if (file == null) {
			return;
		}
		ConnectBuilder.deleteFile(file.getId());
		if (DEBUG) {
			LogUtil.v(TAG, "deleteExistFromServer " + file);
		}
	}

	// /////////////////////check method ///////////////////////
	public boolean checkTask(CreateTask task) {
		if (task == null) return false;
		return checkId(task.albumId, task.batchId, task.index);
	}

	public boolean chedkId(String hash) {
		if (hash == null) {
			return false;
		}
		for (FileEntity fileEntity : cancleLinkedList) {
			if (hash.equals(fileEntity.getFakeId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * only use for ConnectBuilder::createFile()
	 * 
	 * @param params
	 * @return
	 */
	public boolean checkJosnParams(String params) {
		String hash = Parser.parseFileFakeId(params);
		return chedkId(hash);
	}

	/**
	 * only use for ConnectBuilder::uploadFile
	 * 
	 * @param fileEntity
	 * @return
	 */
	public boolean checkFileEntity(String fileEntity) {
		FileEntity file = Parser.parseFile(fileEntity);
		return checkFileEntity(file);
	}

	/**
	 * use for EventService::createFile EventService::createFile
	 * AlbumDetailActivity::onFileUploadDone
	 * 
	 * @param fileEntity
	 * @return T in cancel list
	 */
	public boolean checkFileEntity(FileEntity fileEntity) {
		if (fileEntity == null) {
			return false;
		}
		return checkId(fileEntity.getAlbum(), fileEntity.getBatchId(), fileEntity.getSeqNum());
	}

	/**
	 * check give info is in cancleLindedList
	 * 
	 * @param albumId
	 * @param batchId
	 * @param seqNum
	 * @return T in cancel List F not in cancel list
	 */
	public boolean checkId(String albumId, String batchId, String seqNum) {
		String hash = Utils.createHashId(albumId, batchId, seqNum);
		return chedkId(hash);
	}

	/**
	 * check give info is in cancleLindedList
	 * 
	 * @param albumId
	 * @param batchId
	 * @param seqNum
	 * @return T in cancel List F not in cancel list
	 */
	public boolean checkId(String albumId, String batchId, int seqNum) {
		return checkId(albumId, batchId, String.valueOf(seqNum));
	}

	// //////tool method ///////
	private static boolean checkParams(String... params) {
		for (String param : params) {
			if (Utils.isEmpty(param)) {
				return false;
			}
		}
		return true;
	}
}
