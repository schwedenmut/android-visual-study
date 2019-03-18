package de.schwedenmut.mobileinformationstudy.misc

import android.os.AsyncTask

class MyAsyncTask<T>(val f: () -> T, val r: ((T) -> Unit)? = null) : AsyncTask<Unit, Unit, T>() {
    override fun doInBackground(vararg params: Unit?): T = f()
    override fun onPostExecute(result: T) {
        r?.invoke(result)
    }
}
