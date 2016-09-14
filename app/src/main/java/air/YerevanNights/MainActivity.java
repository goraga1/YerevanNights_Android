package air.YerevanNights;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener {
    private static String ARM_URL = "http://radio.yerevannights.com:81/YerevanNights";
    private static String RUS_URL = "http://radio.yerevannights.com:81/Russian";
    private static String EAU_URL = "http://radio.yerevannights.com:81/European";
    private static String ARM_URL_HD = "http://radio.yerevannights.com:81/YerevanNights.aac";
    private static String RUS_URL_HD = "http://radio.yerevannights.com:81/Russian.aac";
    private static String EAU_URL_HD = "http://radio.yerevannights.com:81/European.aac";
    Point p;
    private MediaPlayer mediaPlayer;
    private boolean hd_on = false;
    private ImageView playButton;
    private ImageView stopButton;
    private URL url;
    ImageButton russianButton;
    ImageButton armenianButton;
    ImageButton europeanButton;
    TextView marqueText;
    ImageView arrowRight;
    String streamingUrl = "http://radio.yerevannights.com:81/YerevanNights";
    String streamingUrlHD = "http://radio.yerevannights.com:81/YerevanNights.aac";
    TextView streaming;
    ImageView imageIcon;

    private ProgressBar loadingSpinner;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        streaming = (TextView) findViewById(R.id.streaming);
        marqueText = (TextView) findViewById(R.id.marqueeText);

        /**** AD VIEW *****/


        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        // Create an ad.
        // "ca-app-pub-1903586528289428/5159323390");

        // Add the AdView to the view hierarchy. The view will have no size
        // until the ad is loaded.

        RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lay.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);


        initControls();

        if (isOnline() == false) {

            WifiManager wifiManager = (WifiManager) this
                    .getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            // wifiManager.setWifiEnabled(false);
            Toast netToast = Toast.makeText(getApplicationContext(),
                    "Please check your internet connection", Toast.LENGTH_LONG);
            netToast.show();
        }

        ImageView arrowLeft = (ImageView) findViewById(R.id.arrowLeft);
        arrowRight = (ImageView) findViewById(R.id.arrowRight);

        /****************************** OPEN LEFT POPUP ********************************/
        arrowLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (p != null)
                    showPopup(MainActivity.this, p);

            }
        });
        /****************************** OPEN RIGHT POPUP ********************************/
        arrowRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (p != null)
                    showPopup1(MainActivity.this, p);

            }
        });

        final ToggleButton hd_btn = (ToggleButton) findViewById(R.id.on_btn);


        hd_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                stopPlaying();


            }
        });


    }

    private void initControls() {

        playButton = (ImageView) findViewById(R.id.play);
        stopButton = (ImageView) findViewById(R.id.stop);

        playButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {


                startStreamingAudio();

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (ARM_URL.equalsIgnoreCase(streamingUrl)) {
                                    new XmlParsing().execute();
                                }
                            }
                        });
                    }
                }, 0, 10000);


                playButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.INVISIBLE);
                loadingSpinner.setVisibility(View.VISIBLE);
            }
        });

        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying()) {
                    try {
                        stopPlaying();
                    } catch (Exception ex) {
                        ex.getStackTrace();
                    }

                }
            }
        });
    }

    private void startStreamingAudio() {

        Runnable r = new Runnable() {

            public void run() {
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnPreparedListener(MainActivity.this);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(streamingUrl);
                    mediaPlayer.prepare();

                    // mediaPlayer.prepareAsync();
                    //  mediaPlayer.start();


                } catch (IOException e) {
                    Log.e(getClass().getName(),
                            "Unable to initialize the MediaPlayer for fileUrl="
                                    + streamingUrl, e);
                    return;
                }
            }
        };
        new Thread(r).start();

    }

    /******************************
     * Check Internet Connection
     ********************************/

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    /******************************
     * POPUP LEFT ARROW
     ********************************/
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        int[] location = new int[2];
        p = new Point();
        p.x = location[0];
        p.y = location[1];
    }

    // The method that displays the popup.
    private void showPopup(final Activity context, Point p) {
        int popupWidth;
        int popupHeight;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        popupWidth = (int) (size.x / 1.2);
        popupHeight = (int) (size.y / 1.5);

        RelativeLayout viewGroup = (RelativeLayout) context
                .findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) RelativeLayout.inflate(this,
                R.layout.popup_left, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        // Some offset to align the popup a bit to the right, and a bit down,
        // relative to button's position.
        int OFFSET_X = 0;
        int OFFSET_Y = 0;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.BOTTOM, p.x + OFFSET_X, p.y
                + OFFSET_Y);

        // Getting a reference to Close button, and close the popup when
        // clicked.
        ImageButton close = (ImageButton) layout.findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        final ImageView header = (ImageView) findViewById(R.id.header);
        russianButton = (ImageButton) layout.findViewById(R.id.buttonRussian);

        armenianButton = (ImageButton) layout.findViewById(R.id.buttonArmenian);
        europeanButton = (ImageButton) layout.findViewById(R.id.buttonEuropean);

        russianButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                header.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.russian_header));
                arrowRight.setVisibility(View.INVISIBLE);
                marqueText.setText("");


                stopPlaying();


                streamingUrl = RUS_URL;
                streamingUrlHD = RUS_URL_HD;
                popup.dismiss();

            }
        });

        europeanButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                header.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.european_header));
                arrowRight.setVisibility(View.INVISIBLE);
                marqueText.setText("");

                stopPlaying();


                streamingUrl = EAU_URL;
                streamingUrlHD = EAU_URL_HD;
                popup.dismiss();

            }
        });

        armenianButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                header.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.main_logo));
                arrowRight.setVisibility(View.VISIBLE);
                header.setVisibility(View.VISIBLE);
                marqueText.setText("");

                if (mediaPlayer.isPlaying()) {
                    stopPlaying();
                }
                popup.dismiss();


                streamingUrl = ARM_URL;
                streamingUrlHD = ARM_URL_HD;

            }
        });

    }

    ;

    private void showPopup1(final Activity context, Point p1) {
        int popupWidth;
        int popupHeight;
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        popupWidth = (int) (size.x / 1.2);
        popupHeight = (int) (size.y / 1.5);


        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) context
                .findViewById(R.id.popupRight);
        LayoutInflater layoutInflater1 = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout1 = (RelativeLayout) RelativeLayout.inflate(this,
                R.layout.popup_right, viewGroup);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout1);
        popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);
        popup.setFocusable(true);

        int OFFSET_X = 0;
        int OFFSET_Y = 0;

        popup.setBackgroundDrawable(new BitmapDrawable());

        popup.showAtLocation(layout1, Gravity.BOTTOM, p.x + OFFSET_X, p.y
                + OFFSET_Y);

        ImageButton close = (ImageButton) layout1.findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        ImageButton dedicate = (ImageButton) layout1
                .findViewById(R.id.buttonDedicate);
        dedicate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DedicateActivity.class);
                startActivity(i);
            }
        });

        ImageButton playlist = (ImageButton) layout1
                .findViewById(R.id.buttonPlaylist);
        playlist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PlaylistActivity.class);
                startActivity(i);
                /*
                 * overridePendingTransition(R.anim.slide_in_up,
				 * R.anim.slide_out_up);
				 */
                ;
            }
        });

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        if (mediaPlayer.isPlaying()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    playButton.setVisibility(View.INVISIBLE);
                    stopButton.setVisibility(View.VISIBLE);
                    loadingSpinner.setVisibility(View.INVISIBLE);
                    streaming.setVisibility(View.VISIBLE);
                }
            });
        }
    }


    public class XmlParsing extends AsyncTask<Void, Void, Bitmap> {

        private URLConnection urlCon;
        private InputStream inputXml;
        // private ImageView bmImage;
        private String artist;
        private String title;
        private String trackno;
        private String minutes;
        private String seconds;
        private String album;
        private String albumyear;

        protected Bitmap doInBackground(Void... Params) {

            try {
                if (url == null)
                    url = new URL(
                            "http://www.yerevannights.com//includes/xml/now_playing.xml");
                if (urlCon == null)
                    urlCon = url.openConnection();

                inputXml = urlCon.getInputStream();

                DocumentBuilderFactory factory = DocumentBuilderFactory
                        .newInstance();

                // DocumentBuilderFactory factory =
                // DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                DocumentBuilder builder = factory.newDocumentBuilder();
                builder.setEntityResolver(new EntityResolver() {
                    @Override
                    public InputSource resolveEntity(String arg0, String arg1)
                            throws SAXException, IOException {
                        if (arg0.contains("Hibernate")) {
                            return new InputSource(new StringReader(""));
                        } else {
                            // TODO Auto-generated method stub
                            return null;
                        }
                    }
                });
                Document doc = builder.parse(inputXml);

                // DocumentBuilder builder = factory.newDocumentBuilder();
                // Document doc = builder.parse(inputXml);
                NodeList nodi = doc.getElementsByTagName("row");

                if (nodi.getLength() > 0) {

                    Element nodo = (Element) nodi.item(0);
                    artist = nodo.getAttribute("Artist");

                    Element nodo1 = (Element) nodi.item(0);
                    title = nodo1.getAttribute("Title");

                    Element nodo2 = (Element) nodi.item(0);
                    trackno = nodo2.getAttribute("TrackNo");

                    Element nodo3 = (Element) nodi.item(0);
                    minutes = nodo3.getAttribute("Minutes");

                    Element nodo4 = (Element) nodi.item(0);
                    seconds = nodo4.getAttribute("Seconds");

                    Element nodo5 = (Element) nodi.item(0);
                    album = nodo5.getAttribute("Album");

                    Element nodo6 = (Element) nodi.item(0);
                    albumyear = nodo6.getAttribute("AlbumYear");

                    runOnUiThread(new Runnable() {
                        public void run() {

                            ;
                            marqueText.setText(artist + "-" + trackno + "."
                                    + " " + title + "(" + minutes + ":"
                                    + seconds + ")" + "-" + album + "("
                                    + albumyear + ")" + "-"
                                    + "YerevanNights.com");

                            // new DownloadImageTask(
                            // (ImageView) findViewById(R.id.image))
                            // .execute();

                        }
                    });

                    System.out.println(artist + "-" + trackno + "." + " "
                            + title + "(" + minutes + ":" + seconds + ")" + "-"
                            + album + "(" + albumyear + ")" + "-"
                            + "YerevanNights.com");

                }
            } catch (MalformedURLException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();

            } catch (SAXException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (inputXml != null)
                        inputXml.close();
                } catch (IOException e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
            return getImgByUrl((String[]) null);

        }

        protected void onPostExecute(Bitmap feed) {
            imageIcon = (ImageView) findViewById(R.id.image);
            imageIcon.setImageBitmap(feed);
        }

        private Bitmap getImgByUrl(String... urls) {
            String urldisplay = "http://www.yerevannights.com/photos/albums/"
                    + artist + " - " + album + ".gif";
            urldisplay = urldisplay.replaceAll(" ", "%20");

            Bitmap mIcon11 = null;
            try {
                InputStream in = new URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        stopPlaying();
        super.onDestroy();
    }


    private void stopPlaying() {
        if (mediaPlayer != null) {
            marqueText.setText("");
            imageIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.placeholder));
            playButton.setVisibility(View.VISIBLE);
            stopButton.setVisibility(View.INVISIBLE);
            loadingSpinner.setVisibility(View.INVISIBLE);
            streaming.setVisibility(View.INVISIBLE);
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
