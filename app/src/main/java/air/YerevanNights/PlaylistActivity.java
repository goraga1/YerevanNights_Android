package air.YerevanNights;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class PlaylistActivity extends Activity {

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_webview);
		final ProgressDialog pdialog = new ProgressDialog(PlaylistActivity.this);
		pdialog.setCancelable(true);
		pdialog.setMessage("Loading ....");
		pdialog.show();

		final WebView webView = (WebView) findViewById(R.id.webView);

		webView.setInitialScale(1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setScrollbarFadingEnabled(false);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDisplayZoomControls(true);

		webView.loadUrl("http://www.yerevannights.com/playlist.asp");

		webView.setWebViewClient(new WebViewClient() {

			public void onPageFinished(WebView view, String url) {
				pdialog.dismiss();
			}
		});

		Button webBack = (Button) findViewById(R.id.webBack);

		webBack.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				webView.goBack();
			}
		});
		Button webForward = (Button) findViewById(R.id.webForward);

		webForward.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				webView.goForward();
			}
		});
		Button goPlaylist = (Button) findViewById(R.id.goPlaylist);

		goPlaylist.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				PlaylistActivity.this.finish();
				overridePendingTransition(R.anim.slide_in_up,
						R.anim.slide_out_up);
			}
		});

	}

	public void onPause() {
		super.onPause();
		overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
	}

	public void OnBackPressed() {

		// Intent i = new Intent(DedicateActivity.this, DedicateActivity.class);
		// startActivity(i);
		PlaylistActivity.this.finish();

	}

}
