package com.mypodcasts.player;

import android.app.Notification;
import android.content.Intent;

import com.google.inject.AbstractModule;
import com.mypodcasts.BuildConfig;
import com.mypodcasts.podcast.models.Episode;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import de.greenrobot.event.EventBus;

import static com.mypodcasts.player.AudioPlayerService.ONGOING_NOTIFICATION_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Robolectric.buildService;
import static org.robolectric.RuntimeEnvironment.application;
import static roboguice.RoboGuice.Util.reset;
import static roboguice.RoboGuice.overrideApplicationInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AudioPlayerServiceTest {
  AudioPlayerService service;
  Episode episode = new Episode();

  AudioPlayerStreaming audioPlayerStreamingMock = mock(AudioPlayerStreaming.class);
  Notification.Builder notificationBuilderMock = mock(Notification.Builder.class);
  Notification notificationMock = mock(Notification.class);
  private EventBus eventBusMock = mock(EventBus.class);

  @Before
  public void setup() {
    overrideApplicationInjector(application, new MyTestModule());

    when(notificationBuilderMock.setContentTitle(
        (CharSequence) anyObject())
    ).thenReturn(notificationBuilderMock);
    when(notificationBuilderMock.setContentText(
            (CharSequence) anyObject())
    ).thenReturn(notificationBuilderMock);

    when(notificationBuilderMock.build())
        .thenReturn(notificationMock);
  }

  @After
  public void teardown() {
    reset();
  }

  @Test
  public void itSetsNotificationContentTitle() {
    createService();

    verify(notificationBuilderMock).setContentTitle("My Podcasts");
  }

  @Test
  public void itSetsNotificationContentText() {
    createService();

    verify(notificationBuilderMock).setContentText("Some awesome podcast!");
  }

  @Test
  @Ignore
  public void itStartsOnForeground() {
    createService();

    verify(service).startForeground(ONGOING_NOTIFICATION_ID, notificationMock);
  }

  @Test
  public void itPlaysAudioStreamingGivenAnEpisode() throws IOException {
    createService();

    verify(audioPlayerStreamingMock).play(episode);
  }

  @Test
  public void itBroadcastsEventThatAudioIsPlaying() {
    createService();

    verify(eventBusMock).post(any(AudioPlayingEvent.class));
  }

  @Test
  public void itReleasesAudioPlayerOnDestroy() {
    Intent intent = getIntent();
    service = buildService(AudioPlayerService.class).withIntent(intent).create().destroy().get();

    verify(audioPlayerStreamingMock).release();
  }

  @Test
  public void itReturnsIfIsPlaying() {
    createService();

    service.isPlaying();

    verify(audioPlayerStreamingMock).isPlaying();
  }

  @Test
  public void itPausesIfPlaying() {
    createService();
    when(audioPlayerStreamingMock.isPlaying()).thenReturn(true);

    service.togglePlayPauseFor(episode);

    verify(audioPlayerStreamingMock).pause();
  }

  @Test
  public void itUnPausesIfNotPlaying() {
    createService();
    when(audioPlayerStreamingMock.isPlaying()).thenReturn(false);

    service.togglePlayPauseFor(episode);

    verify(audioPlayerStreamingMock).unPause(episode);
  }

  private void createService() {
    Intent intent = getIntent();

    service = spy(
        buildService(AudioPlayerService.class)
            .withIntent(intent)
            .create()
            .startCommand(0, 1)
            .get()
    );
  }

  private Intent getIntent() {
    Intent intent = new Intent(application, AudioPlayerService.class);
    intent.putExtra(Episode.class.toString(), episode);
    return intent;
  }

  public class MyTestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(EventBus.class).toInstance(eventBusMock);
      bind(AudioPlayerStreaming.class).toInstance(audioPlayerStreamingMock);
      bind(Notification.Builder.class).toInstance(notificationBuilderMock);
    }
  }
}