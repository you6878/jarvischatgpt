package com.howlab.jarvischatgpt

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), PermissionCallbacks {
    /**
     * Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
     */
    private var mService: Calendar? = null

    /**
     * Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
     */
    private var mID = 0
    var mCredential: GoogleAccountCredential? = null
    private var mStatusText: TextView? = null
    private var mResultText: TextView? = null
    private var mGetEventButton: Button? = null
    private var mAddEventButton: Button? = null
    private var mAddCalendarButton: Button? = null
    var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAddCalendarButton = findViewById<View>(R.id.button_main_add_calendar) as Button
        mAddEventButton = findViewById<View>(R.id.button_main_add_event) as Button
        mGetEventButton = findViewById<View>(R.id.button_main_get_event) as Button
        mStatusText = findViewById<View>(R.id.textview_main_status) as TextView
        mResultText = findViewById<View>(R.id.textview_main_result) as TextView
        /**
         * 버튼 클릭으로 동작 테스트
         */
        mAddCalendarButton?.setOnClickListener {
            mAddCalendarButton?.isEnabled = false
            mStatusText?.text = ""
            mID = 1 //캘린더 생성
            resultsFromApi
            mAddCalendarButton?.isEnabled = true
        }
        mAddEventButton?.setOnClickListener {
            mAddEventButton?.isEnabled = false
            mStatusText?.text = ""
            mID = 2 //이벤트 생성
            resultsFromApi
            mAddEventButton?.isEnabled = true
        }
        mGetEventButton?.setOnClickListener {
            mGetEventButton?.isEnabled = false
            mStatusText?.text = ""
            mID = 3 //이벤트 가져오기
            resultsFromApi
            mGetEventButton?.isEnabled = true
        }


        // Google Calendar API의 호출 결과를 표시하는 TextView를 준비
        mResultText?.isVerticalScrollBarEnabled = true
        mResultText?.movementMethod = ScrollingMovementMethod()
        mStatusText?.isVerticalScrollBarEnabled = true
        mStatusText?.movementMethod = ScrollingMovementMethod()
        mStatusText?.text = "버튼을 눌러 테스트를 진행하세요."


        // Google Calendar API 호출중에 표시되는 ProgressDialog
        mProgress = ProgressDialog(this)
        mProgress?.setMessage("Google Calendar API 호출 중입니다.")


        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext,
            Arrays.asList(*SCOPES)
        ).setBackOff(ExponentialBackOff()) // I/O 예외 상황을 대비해서 백오프 정책 사용
    }// Google Calendar API 호출// 인터넷을 사용할 수 없는 경우// 유효한 Google 계정이 선택되어 있지 않은 경우// Google Play Services를 사용할 수 없는 경우

    /**
     * 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
     *
     * 사전 조건
     * - Google Play Services 설치
     * - 유효한 구글 계정 선택
     * - 안드로이드 디바이스에서 인터넷 사용 가능
     *
     * 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.
     */
    private val resultsFromApi: String?
        private get() {
            if (!isGooglePlayServicesAvailable) { // Google Play Services를 사용할 수 없는 경우
                acquireGooglePlayServices()
            } else if (mCredential?.selectedAccountName == null) { // 유효한 Google 계정이 선택되어 있지 않은 경우
                chooseAccount()
            } else if (!isDeviceOnline) {    // 인터넷을 사용할 수 없는 경우
                mStatusText?.text = "No network connection available."
            } else {

                // Google Calendar API 호출
                MakeRequestTask(this, mCredential).execute()
            }
            return null
        }

    /**
     * 안드로이드 디바이스에 최신 버전의 Google Play Services가 설치되어 있는지 확인
     */
    private val isGooglePlayServicesAvailable: Boolean
        private get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    /*
   * Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 유도하기위해
   * 대화상자를 보여줌.
   */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /*
    * 안드로이드 디바이스에 Google Play Services가 설치 안되어 있거나 오래된 버전인 경우 보여주는 대화상자
    */
    fun showGooglePlayServicesAvailabilityErrorDialog(
        connectionStatusCode: Int
    ) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@MainActivity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog?.show()
    }

    /*
    * Google Calendar API의 자격 증명( credentials ) 에 사용할 구글 계정을 설정한다.
    *
    * 전에 사용자가 구글 계정을 선택한 적이 없다면 다이얼로그에서 사용자를 선택하도록 한다.
    * GET_ACCOUNTS 퍼미션이 필요하다.
    */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {

        // GET_ACCOUNTS 권한을 가지고 있다면
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {


            // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            val accountName = getPreferences(MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {

                // 선택된 구글 계정 이름으로 설정한다.
                mCredential?.selectedAccountName = accountName
                resultsFromApi
            } else {


                // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
                mCredential?.newChooseAccountIntent()?.let {
                    startActivityForResult(
                        it,
                        REQUEST_ACCOUNT_PICKER
                    )
                }
            }


            // GET_ACCOUNTS 권한을 가지고 있지 않다면
        } else {


            // 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
            EasyPermissions.requestPermissions(
                (this as Activity),
                "This app needs to access your Google account (via Contacts).",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    /*
    * 구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
    */
    override fun onActivityResult(
        requestCode: Int,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
        resultCode: Int,  // 요청에 대한 결과 코드
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != RESULT_OK) {
                mStatusText?.text = (" 앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
                        + "구글 플레이 서비스를 설치 후 다시 실행하세요.")
            } else {
                resultsFromApi
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = getPreferences(MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    editor.apply()
                    mCredential?.selectedAccountName = accountName
                    resultsFromApi
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == RESULT_OK) {
                resultsFromApi
            }
        }
    }

    /*
    * Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
        permissions: Array<String>,  // 요청한 퍼미션
        grantResults: IntArray // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /*
    * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다.
    */
    override fun onPermissionsGranted(requestCode: Int, requestPermissionList: List<String>) {

        // 아무일도 하지 않음
    }

    /*
    * EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다.
    */
    override fun onPermissionsDenied(requestCode: Int, requestPermissionList: List<String>) {

        // 아무일도 하지 않음
    }

    /*
   * 안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
   */
    private val isDeviceOnline: Boolean
        private get() {
            val connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    /*
   * 캘린더 이름에 대응하는 캘린더 ID를 리턴
   */
    private fun getCalendarID(calendarTitle: String): String? {
        var id: String? = null

        // Iterate through entries in calendar list
        var pageToken: String? = null
        do {
            var calendarList: CalendarList? = null
            try {
                calendarList = mService?.calendarList()?.list()?.setPageToken(pageToken)?.execute()
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val items = calendarList?.items ?: arrayListOf()
            for (calendarListEntry in items) {
                if (calendarListEntry.summary.toString() == calendarTitle) {
                    id = calendarListEntry.id.toString()
                }
            }
            pageToken = calendarList?.nextPageToken
        } while (pageToken != null)
        return id
    }

    /*
    * 비동기적으로 Google Calendar API 호출
    */
    private inner class MakeRequestTask(
        private val mActivity: MainActivity,
        credential: GoogleAccountCredential?
    ) :
        AsyncTask<Void?, Void?, String?>() {
        private var mLastError: Exception? = null
        var eventStrings: MutableList<String?> = ArrayList()

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build()
        }

        override fun onPreExecute() {
            // mStatusText.setText("");
            mProgress?.show()
            mStatusText?.text = "데이터 가져오는 중..."
            mResultText?.text = ""
        }

        /*
        * 백그라운드에서 Google Calendar API 호출 처리
        */
        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (mID == 1) {
                    return createCalendar()
                } else if (mID == 2) {
                    return addEvent()
                } else if (mID == 3) {
                    return event
                }
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                return null
            }
            return null
        }// 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용//"primary")

        //.setTimeMin(now)
        /*
        * CalendarTitle 이름의 캘린더에서 10개의 이벤트를 가져와 리턴
        */
        @get:Throws(IOException::class)
        private val event: String
            private get() {
                val now = DateTime(System.currentTimeMillis())
                val calendarID = getCalendarID("CalendarTitle") ?: return "캘린더를 먼저 생성하세요."
                val events = mService?.events()?.list(calendarID) //"primary")
                    ?.setMaxResults(10) //.setTimeMin(now)
                    ?.setOrderBy("startTime")
                    ?.setSingleEvents(true)
                    ?.execute()
                val items = events?.items ?: arrayListOf()
                for (event in items) {
                    var start = event.start.dateTime
                    if (start == null) {

                        // 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용
                        start = event.start.date
                    }
                    eventStrings.add(String.format("%s \n (%s)", event.summary, start))
                }
                return eventStrings.size.toString() + "개의 데이터를 가져왔습니다."
            }

        /*
        * 선택되어 있는 Google 계정에 새 캘린더를 추가한다.
        */
        @Throws(IOException::class)
        private fun createCalendar(): String {
            val ids = getCalendarID("CalendarTitle")
            if (ids != null) {
                return "이미 캘린더가 생성되어 있습니다. "
            }

            // 새로운 캘린더 생성
            val calendar = com.google.api.services.calendar.model.Calendar()

            // 캘린더의 제목 설정
            calendar.summary = "CalendarTitle"


            // 캘린더의 시간대 설정
            calendar.timeZone = "Asia/Seoul"

            // 구글 캘린더에 새로 만든 캘린더를 추가
            val createdCalendar = mService?.calendars()?.insert(calendar)?.execute()

            // 추가한 캘린더의 ID를 가져옴.
            val calendarId = createdCalendar?.id


            // 구글 캘린더의 캘린더 목록에서 새로 만든 캘린더를 검색
            val calendarListEntry = mService?.calendarList()?.get(calendarId)?.execute()

            // 캘린더의 배경색을 파란색으로 표시  RGB
            calendarListEntry?.backgroundColor = "#0000ff"

            // 변경한 내용을 구글 캘린더에 반영
            val updatedCalendarListEntry = mService?.calendarList()
                ?.update(calendarListEntry?.id, calendarListEntry)
                ?.setColorRgbFormat(true)
                ?.execute()

            // 새로 추가한 캘린더의 ID를 리턴
            return "캘린더가 생성되었습니다."
        }

        override fun onPostExecute(output: String?) {
            mProgress?.hide()
            mStatusText?.text = output
            if (mID == 3) mResultText?.text = TextUtils.join("\n\n", eventStrings)
        }

        override fun onCancelled() {
            mProgress?.hide()
            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                        (mLastError as GooglePlayServicesAvailabilityIOException)
                            .connectionStatusCode
                    )
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                        (mLastError as UserRecoverableAuthIOException).intent,
                        REQUEST_AUTHORIZATION
                    )
                } else {
                    mStatusText?.text = """
                        MakeRequestTask The following error occurred:
                        ${mLastError?.message}
                        """.trimIndent()
                }
            } else {
                mStatusText?.text = "요청 취소됨."
            }
        }

        private fun addEvent(): String {
            val calendarID = getCalendarID("CalendarTitle") ?: return "캘린더를 먼저 생성하세요."
            var event =
                Event()
                    .setSummary("구글 캘린더 테스트")
                    .setLocation("서울시")
                    .setDescription("캘린더에 이벤트 추가하는 것을 테스트합니다.")
            val calander: java.util.Calendar
            calander = java.util.Calendar.getInstance()
            val simpledateformat: SimpleDateFormat
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA);
            // Z에 대응하여 +0900이 입력되어 문제 생겨 수작업으로 입력
            simpledateformat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA)
            val datetime = simpledateformat.format(calander.time)
            val startDateTime =
                DateTime(datetime)
            val start = EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul")
            event.start = start
            Log.d("@@@", datetime)
            val endDateTime =
                DateTime(datetime)
            val end = EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul")
            event.end = end

            //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
            //event.setRecurrence(Arrays.asList(recurrence));
            try {
                event = mService?.events()?.insert(calendarID, event)?.execute()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Exception", "Exception : $e")
            }
            System.out.printf("Event created: %s\n", event.htmlLink)
            Log.e("Event", "created : " + event.htmlLink)
            return "created : " + event.htmlLink
        }
    }

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        private const val PREF_ACCOUNT_NAME = "accountName"
        private val SCOPES = arrayOf(CalendarScopes.CALENDAR)
    }
}