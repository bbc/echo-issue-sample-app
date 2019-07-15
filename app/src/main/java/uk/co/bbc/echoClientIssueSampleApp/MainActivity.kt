package uk.co.bbc.echoClientIssueSampleApp

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import uk.co.bbc.echo.EchoClient
import uk.co.bbc.echo.EchoConfigKeys
import uk.co.bbc.echo.enumerations.ApplicationType
import uk.co.bbc.echo.enumerations.Destination
import uk.co.bbc.echo.interfaces.Echo

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val echo = createEcho(this)

        SmpLauncher(this, echo).launch("b05w8l03")
    }

    private fun createEcho(context: Context): Echo {
        val params = hashMapOf(
            EchoConfigKeys.COMSCORE_ENABLED to "false",
            EchoConfigKeys.ATI_ENABLED to "true",
            EchoConfigKeys.ECHO_ENABLED to "true",
            EchoConfigKeys.DESTINATION to Destination.IPLAYER.toString(),
            EchoConfigKeys.IDV5_ENABLED to "true",
            EchoConfigKeys.BARB_ENABLED to "true",
            EchoConfigKeys.BARB_SITE_CODE to "bbcandroid",
            EchoConfigKeys.USE_ESS to "true",
            EchoConfigKeys.ECHO_DEBUG to "true",
            EchoConfigKeys.KEEPALIVE_DURATION to "1000"
        )

        return EchoClient(
            "IssueSampleApp",
            ApplicationType.MOBILE_APP,
            "test",
            context,
            params,
            null
        )
    }
}
