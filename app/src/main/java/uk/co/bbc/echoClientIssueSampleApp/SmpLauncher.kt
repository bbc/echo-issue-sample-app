package uk.co.bbc.echoClientIssueSampleApp

import android.content.Context
import uk.co.bbc.echo.Media
import uk.co.bbc.echo.enumerations.MediaAvType
import uk.co.bbc.echo.enumerations.MediaConsumptionMode
import uk.co.bbc.echo.interfaces.Echo
import uk.co.bbc.echo.interfaces.PlayerDelegate
import uk.co.bbc.httpclient.useragent.UserAgent
import uk.co.bbc.mediaselector.MediaSelectorClient
import uk.co.bbc.mediaselector.MediaSelectorClientConfiguration
import uk.co.bbc.mediaselector.MediaSet
import uk.co.bbc.mediaselector.request.MediaSelectorRequestParameters
import uk.co.bbc.smpan.SMP
import uk.co.bbc.smpan.SMPBuilder
import uk.co.bbc.smpan.media.PlayRequest
import uk.co.bbc.smpan.media.model.MediaContentEpisodePid
import uk.co.bbc.smpan.media.model.MediaContentIdentifier
import uk.co.bbc.smpan.media.model.MediaContentVpid
import uk.co.bbc.smpan.media.model.MediaMetadata
import uk.co.bbc.smpan.playercontroller.media.MediaPosition
import uk.co.bbc.smpan.playercontroller.media.MediaProgress
import uk.co.bbc.smpan.stats.av.AVStatisticsProvider
import uk.co.bbc.smpan.stats.av.AppGeneratedAVStatsLabels
import uk.co.bbc.smpan.stats.ui.UserInteractionStatisticsProvider

class SmpLauncher(private val context: Context, private val echo: Echo) : PlayerDelegate {
    var pos: Long = 0
    var hasChanged = false

    override fun getPosition(): Long {
        return pos
    }

    override fun getTimestamp(): Long {
        return pos
    }

    fun launch(vpid: String) {
        val smp = SMPBuilder.create(
            context,
            UserAgent("echoIssueSample", BuildConfig.VERSION_NAME),
            UserInteractionStatisticsProvider.NULL
        ).build()

        val mediaContentIdentifier: MediaContentIdentifier = vpidToMediaContentIdentifier(vpid)

        startPlayback(mediaContentIdentifier, smp)
    }

    private fun vpidToMediaContentIdentifier(vpid: String): MediaContentIdentifier {
        val mediaContentIdentifier: MediaContentIdentifier = MediaContentVpid(
            vpid,
            MediaSelectorClient.MediaSelectorClientBuilder()
                .withConfig(
                    object : MediaSelectorClientConfiguration {
                        override fun getSecureMediaSelectorBaseUrl(): String {
                            return "https://open.live.bbc.co.uk/mediaselector/6/select"

                        }

                        override fun getDefaultParameters(): MediaSelectorRequestParameters {
                            return MediaSelectorRequestParameters()
                        }

                        override fun getMediaSelectorBaseUrl(): String {
                            return "https://open.live.bbc.co.uk/mediaselector/6/select"
                        }

                        override fun getSecureClientId(): String {
                            return "sample"
                        }

                        override fun getMediaSet(): MediaSet {
                            return MediaSet.fromString("mobile-phone-main")
                        }

                        override fun getUserAgent(): String {
                            return "sample"
                        }

                    }
                ).build()
        )
        return mediaContentIdentifier
    }

    private fun startPlayback(
        mediaContentIdentifier: MediaContentIdentifier,
        smp: SMP
    ) {

        val request = createPlayRequest(mediaContentIdentifier, statsProvider)

        smp.loadFullScreen(request)
        smp.play()
        smp.addProgressListener {
            if (it.positionInMilliseconds > 10000L && !hasChanged) {
                hasChanged = true
                val newEpIdentifier = vpidToMediaContentIdentifier("b05w8lhh")
                val onwardRequest = createPlayRequest(newEpIdentifier, statsProvider)

                smp.loadFullScreen(onwardRequest)
            }
        }
    }

    private fun createPlayRequest(
        mediaContentIdentifier: MediaContentIdentifier,
        statsProvider: AVStatisticsProvider
    ): PlayRequest? {
        return PlayRequest.create(
            mediaContentIdentifier,
            MediaMetadata.MediaType.ONDEMAND,
            MediaMetadata.MediaAvType.VIDEO,
            statsProvider
        ).build()
    }

    private val statsProvider = object : AVStatisticsProvider {

        override fun newSessionStarted(
            expectedPlayerName: String,
            expectedPlayerVersion: String,
            contentVpid: MediaContentIdentifier,
            episodePid: MediaContentEpisodePid,
            audioVideoETC: MediaMetadata.MediaAvType,
            simulcastOnDemandETC: MediaMetadata.MediaType,
            appGeneratedAvStatsLabels: AppGeneratedAVStatsLabels
        ) {
            pos = 0
            echo.viewEvent("countername-$contentVpid", hashMapOf())

            with(echo) {
                setPlayerName(expectedPlayerName)
                setPlayerVersion(expectedPlayerVersion)
                setPlayerDelegate(this@SmpLauncher)
            }

            val media = Media(MediaAvType.VIDEO, MediaConsumptionMode.ON_DEMAND)

            media.apply {
                versionId = contentVpid.toString()
            }

            echo.setMedia(media)


            echo.avUserActionEvent(
                "play", "started",
                0, hashMapOf()
            )
        }

        override fun trackPlayInitiated(mediaProgress: MediaProgress) {
            pos = mediaProgress.positionInMilliseconds
            echo.setMediaLength(mediaProgress.endTimeInMilliseconds)
            echo.avPlayEvent(mediaProgress.positionInMilliseconds, hashMapOf())
        }

        override fun updateProgress(mediaProgress: MediaProgress) {
            pos = mediaProgress.positionInMilliseconds
            echo.avPlayEvent(
                mediaProgress.positionInMilliseconds,
                hashMapOf()
            )
        }

        override fun trackBuffering(mediaProgress: MediaProgress) {
            pos = mediaProgress.positionInMilliseconds
            echo.avBufferEvent(
                mediaProgress.positionInMilliseconds,
                hashMapOf()
            )
        }

        override fun trackPaused(mediaProgress: MediaProgress) {
            pos = mediaProgress.positionInMilliseconds
            echo.avPauseEvent(
                mediaProgress.positionInMilliseconds,
                hashMapOf()
            )
        }

        override fun trackResumed(mediaProgress: MediaProgress) {
            pos = mediaProgress.positionInMilliseconds
            echo.avPlayEvent(
                mediaProgress.positionInMilliseconds,
                hashMapOf()
            )
        }

        override fun trackScrub(
            fromTime: MediaPosition,
            toTime: MediaPosition,
            customParams: Map<String, String>
        ) {
            pos = toTime.toMilliseconds()
            echo.avSeekEvent(
                toTime.toMilliseconds(),
                hashMapOf()
            )
        }

        override fun trackEnd(
            mediaProgress: MediaProgress,
            customParams: Map<String, String>
        ) {
            pos = mediaProgress.positionInMilliseconds
            echo.avEndEvent(
                mediaProgress.positionInMilliseconds,
                hashMapOf()
            )
        }

        override fun trackError(mediaProgress: MediaProgress) {
        }
    }


}
