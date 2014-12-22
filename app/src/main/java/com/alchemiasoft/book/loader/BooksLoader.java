/*
 * Copyright 2014 Simone Casagranda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alchemiasoft.book.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.alchemiasoft.book.model.Book;

import java.util.List;

/**
 * Loader for Books.
 * <p/>
 * Created by Simone Casagranda on 20/12/14.
 */
public class BooksLoader extends AsyncTaskLoader<List<Book>> {

    private List<Book> mData;

    private final boolean mOwned;

    public BooksLoader(Context context, boolean owned) {
        super(context);
        this.mOwned = owned;
    }

    @Override
    public List<Book> loadInBackground() {
        return null;
    }

    @Override
    public void deliverResult(List<Book> data) {
        if (isReset()) {
            releaseData();
            return;
        }
        mData = data;
        super.deliverResult(data);
    }

    @Override

    protected void onStartLoading() {
        super.onStartLoading();
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        releaseData();
    }

    @Override
    public void onCanceled(List<Book> data) {
        super.onCanceled(data);
        releaseData();
    }

    private void releaseData() {
        mData = null;
    }
}
