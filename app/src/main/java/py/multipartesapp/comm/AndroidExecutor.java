package py.multipartes2.comm;

import android.os.AsyncTask;


public class AndroidExecutor implements ICommExecutor {

	public void execute(final ICommExecutorTask task) {
		new AsyncTask<Object,Object,Object>(){
			@Override
			protected Object doInBackground(Object... params) {
				task.doInBackground();
				return null;
			}
			@Override
			protected void onPostExecute(Object result) {
				task.onPostExecute();
			}
		}.execute((Object[])null);
	}

}
