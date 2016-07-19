package com.mypodcasts.player;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mypodcasts.player.notification.AudioPlayerNotification;
import com.mypodcasts.repositories.models.Episode;

import java.io.IOException;

import javax.inject.Inject;

import roboguice.service.RoboService;

import static com.mypodcasts.support.Support.MYPODCASTS_TAG;

public class AudioPlayerService extends RoboService {

  public static final int ONGOING_NOTIFICATION_ID = 1;

  public static final String ACTION_REWIND = "com.mypodcasts.player.action.rewind";
  public static final String ACTION_PAUSE = "com.mypodcasts.player.action.pause";
  public static final String ACTION_PLAY = "com.mypodcasts.player.action.play";
  public static final String ACTION_STOP = "com.mypodcasts.player.action.stop";
  public static final String ACTION_FAST_FORWARD = "com.mypodcasts.player.action.fast_foward";
  public static final int POSITION = 120;

  @Inject
  private Context context;

  @Inject
  private AudioPlayer audioPlayer;

  @Inject
  private AudioPlayerNotification audioPlayerNotification;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    final Episode episode = (Episode) intent.getSerializableExtra(Episode.class.toString());

    Log.d(MYPODCASTS_TAG, toString());

    if (intent.getAction().equalsIgnoreCase(ACTION_PLAY)) {
      Log.d(MYPODCASTS_TAG, intent.getAction());
      startForeground(ONGOING_NOTIFICATION_ID,  audioPlayerNotification.buildNotification(episode));

      try {
        audioPlayer.play(episode);
      } catch (IOException e) {
        Log.e(MYPODCASTS_TAG, e.getMessage());

        e.printStackTrace();
      }
    } else {
      handleMediaControlByAction(intent);
    }

    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    audioPlayer.release();
  }

  private void handleMediaControlByAction(Intent intent) {
    if (intent.getAction() == null) return;

    if(intent.getAction().equalsIgnoreCase(ACTION_REWIND)){
      Log.d(MYPODCASTS_TAG, ACTION_REWIND);

      audioPlayer.seekTo(audioPlayer.getCurrentPosition() - POSITION);
    }

    if(intent.getAction().equalsIgnoreCase(ACTION_PAUSE)){
      Log.d(MYPODCASTS_TAG, ACTION_PAUSE);

      audioPlayer.pause();
    }

    if(intent.getAction().equalsIgnoreCase(ACTION_STOP)){
      Log.d(MYPODCASTS_TAG, ACTION_STOP);

      audioPlayer.pause();
    }

    if(intent.getAction().equalsIgnoreCase(ACTION_FAST_FORWARD)){
      Log.d(MYPODCASTS_TAG, ACTION_FAST_FORWARD);

      audioPlayer.seekTo(audioPlayer.getCurrentPosition() + POSITION);
    }
  }
}