/*
 * Copyright 2015 Simone Casagranda.
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

package com.alchemiasoft.book.service;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.alchemiasoft.book.R;
import com.alchemiasoft.book.activity.HomeActivity;
import com.alchemiasoft.common.content.BookDB;
import com.alchemiasoft.common.model.Book;
import com.alchemiasoft.book.receiver.SuggestionReceiver;

/**
 * IntentService that takes care of suggesting the user to buy a book that he doesn't already own.
 * <p/>
 * Created by Simone Casagranda on 27/12/14.
 */
public class SuggestionService extends IntentService {

    private static final String TAG_LOG = SuggestionService.class.getSimpleName();

    private static final int ID_SUGGESTION = 23;

    private static final String SELECTION = BookDB.Book.OWNED + " = ?";
    private static final String[] SELECT_NOT_OWNED = {String.valueOf(0)};

    public SuggestionService() {
        super(TAG_LOG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG_LOG, "Starting a new book suggestion...");
        final ContentResolver cr = getContentResolver();
        final Cursor c = cr.query(BookDB.Book.CONTENT_URI, null, SELECTION, SELECT_NOT_OWNED, null);
        Book book = null;
        try {
            if (c.moveToNext()) {
                book = Book.oneFrom(c);
            }
        } finally {
            c.close();
        }
        // Showing a notification if a not owned book is found
        if (book != null) {
            Log.d(TAG_LOG, "Found book that can be suggested: " + book);
            final String content = getString(R.string.content_book_suggestion, book.getTitle(), book.getAuthor());
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.ic_launcher).setAutoCancel(true).setContentTitle(getString(R.string.title_book_suggestion)).setContentText(content);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(content));
            builder.setContentIntent(PendingIntent.getActivity(this, 0, HomeActivity.createFor(this, book), PendingIntent.FLAG_UPDATE_CURRENT));

            // ONLY 4 WEARABLE(s)
            final NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
            // SECOND PAGE WITH BOOK DESCRIPTION
            wearableExtender.addPage(new NotificationCompat.Builder(this).setContentTitle(getString(R.string.description))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(book.getDescrition())).build());
            wearableExtender.setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.background));
            // ACTION TO PURCHASE A BOOK FROM A WEARABLE
            final PendingIntent purchaseIntent = PendingIntent.getService(this, 0, BookActionService.IntentBuilder.buy(this, book).notificationId(ID_SUGGESTION)
                    .wearableInput().build(), PendingIntent.FLAG_UPDATE_CURRENT);
            wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_buy, getString(R.string.action_buy), purchaseIntent).build());
            // ACTION TO ADD NOTES VIA VOICE REPLY
            final RemoteInput input = BookActionService.RemoteInputBuilder.create(this).options(R.array.note_options).build();
            final PendingIntent notesIntent = PendingIntent.getService(this, 0, BookActionService.IntentBuilder.addNote(this, book).notificationId(ID_SUGGESTION)
                    .wearableInput().build(), PendingIntent.FLAG_UPDATE_CURRENT);
            wearableExtender.addAction(new NotificationCompat.Action.Builder(R.drawable.ic_action_notes, getString(R.string.action_notes), notesIntent)
                    .addRemoteInput(input).build());
            // Finally extending the notification
            builder.extend(wearableExtender);

            // Sending the notification
            NotificationManagerCompat.from(this).notify(ID_SUGGESTION, builder.build());
        }
        // Completing the Wakeful Intent
        SuggestionReceiver.completeWakefulIntent(intent);
    }
}
