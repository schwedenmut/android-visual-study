package de.schwedenmut.mobileinformationstudy

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import de.schwedenmut.mobileinformationstudy.misc.BitmapHelper
import de.schwedenmut.mobileinformationstudy.misc.Const
import de.schwedenmut.mobileinformationstudy.misc.Utilities
import kotlinx.android.synthetic.main.activity_pager.*
import org.hcilab.projects.nlog.library.misc.Util
import java.io.File
import java.util.*

class PagerActivity : AppCompatActivity() {

    var page = 0
    val evaluator = android.animation.ArgbEvaluator()
    lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    lateinit var indicators: Array<ImageView>
    lateinit var colorList: Array<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        colorList = arrayOf(ContextCompat.getColor(this, R.color.teal50_200), ContextCompat.getColor(this, R.color.mediumaquamarine),
                ContextCompat.getColor(this, R.color.lightskyblue), ContextCompat.getColor(this, R.color.cyan50_700),
                ContextCompat.getColor(this, R.color.bluegray50_400))

        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        indicators = arrayOf(wizard_indicator_0, wizard_indicator_1, wizard_indicator_2, wizard_indicator_3, wizard_indicator_4)
        container.adapter = mSectionsPagerAdapter
        container.currentItem = page

        wizard_btn_next.setOnClickListener {
            page += 1
            container.setCurrentItem(page, true)
        }
        wizard_btn_back.setOnClickListener {
            page -= 1
            container.setCurrentItem(page, true)
        }
        wizard_btn_finish.setOnClickListener {
            getDefaultSharedPreferences(this).edit().putBoolean(Const.WAS_WIZARD_SHOWN, true).apply()
            startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }

        container.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                val colorUpdate: Int = evaluator.evaluate(p1, colorList[p0], colorList[if (p0 == 4) p0 else p0 + 1]) as Int
                container.setBackgroundColor(colorUpdate)
            }

            override fun onPageSelected(p0: Int) {
                page = p0
                updateIndicators(page)
                wizard_btn_next.visibility = if (p0 == 4) View.GONE else View.VISIBLE
                wizard_btn_finish.visibility = if (p0 == 4) View.VISIBLE else View.GONE
                wizard_btn_back.visibility = if(p0 == 0) View.GONE else View.VISIBLE
                container.setBackgroundColor(colorList[p0])
            }
        })
    }

    internal fun updateIndicators(position: Int) {
        for (i in indicators.indices) {
            indicators[i].setBackgroundResource(
                    if (i == position) R.drawable.indicator_selected else R.drawable.indicator_unselected
            )
        }
    }

    class WizardFragment: Fragment() {

        companion object {
            private const val ARG_SECTION_NUMBER = "section_number"
            private const val REQUEST_PROJECTION = 21
            private lateinit var mediaProjectionManager: MediaProjectionManager
            val bg: Array<Int> = arrayOf(R.drawable.ic_introduction_background, R.drawable.ic_notification_background, R.drawable.ic_screenshot_background, R.drawable.ic_export_background)
            fun newInstance(sectionNumber: Int): WizardFragment {
                val fragment = WizardFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }
        private lateinit var imageView: ImageView
        private lateinit var mFile: File

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val rootView = if (arguments?.getInt(ARG_SECTION_NUMBER) != 4) inflater.inflate(R.layout.content_wizard_fragment, container, false) else inflater.inflate(R.layout.content_wizard_image_fragment, container, false)
            val exportPath = File(context!!.cacheDir, "share")
            if (!exportPath.exists()) exportPath.mkdirs()
            mFile = File(exportPath,  "Testscreenshot.jpeg")
            when (arguments?.getInt(ARG_SECTION_NUMBER)) {
                0 -> rootView.findViewById<TextView>(R.id.text_description).text = fromHtml(R.string.introduction_description)
                1 -> rootView.findViewById<TextView>(R.id.text_description).text = fromHtml(R.string.notification_description)
                2 -> rootView.findViewById<TextView>(R.id.text_description).text = fromHtml(R.string.screenshot_description)
                3 -> rootView.findViewById<TextView>(R.id.text_description).text = fromHtml(R.string.export_description)
                4 -> {
                    rootView.findViewById<TextView>(R.id.text_description).text = fromHtml(R.string.picture_description)
                    imageView = rootView.findViewById(R.id.section_img)
                    mediaProjectionManager = activity!!.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    rootView.findViewById<Button>(R.id.wizard_btn_wrong).setOnClickListener {
                        val intent = Intent(Intent.ACTION_SEND)
                                .setType("image/*")
                                .putExtra(Intent.EXTRA_EMAIL, "marc.schwede@smail.th-koeln.de")
                                .putExtra(Intent.EXTRA_SUBJECT, "Fehler in der Darstellung des Screenshots für die Studie")
                                .putExtra(Intent.EXTRA_TEXT, "${BuildConfig.VERSION_CODE}, ${Util.getLocale(context)}, ${Build.MODEL}, " +
                                        "${Build.DEVICE}, ${Build.PRODUCT}, ${Build.MANUFACTURER}, ${Build.VERSION.SDK_INT}, " +
                                        "${TimeZone.getDefault().id}, ${TimeZone.getDefault().getOffset(System.currentTimeMillis())}, " +
                                        "${System.currentTimeMillis()}")
                                .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(),"de.schwedenmut.mobileinformationstudy.fileprovider", mFile))
                        startActivity(Intent.createChooser(intent, "Send E-Mail ..."))
                    }

                    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_PROJECTION)


                }
            }
            if (arguments!!.getInt(ARG_SECTION_NUMBER) != 4) rootView.findViewById<ImageView>(R.id.section_img).setBackgroundResource(bg[arguments!!.getInt(ARG_SECTION_NUMBER)])
            return rootView
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PROJECTION) getScreenshot(data)
            }
            super.onActivityResult(requestCode, resultCode, data)
        }

        private fun getScreenshot(data: Intent?) {
            val mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data!!)
            val display = (activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            display.getRealSize(size)
            val displayHeight = size.y
            val displayWidth = size.x
            val mImageReader = ImageReader.newInstance(displayWidth, displayHeight, PixelFormat.RGBA_8888, 15)
            mImageReader.setOnImageAvailableListener( ImageReader.OnImageAvailableListener {
                val bitmap = BitmapHelper().convertImageToBitmap(it.acquireLatestImage()!!)
                imageView.setImageBitmap(bitmap)
                Utilities.saveBitmap(bitmap, mFile, Bitmap.CompressFormat.JPEG)
                mediaProjection.stop()
            },null)
            val mVirtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", displayWidth, displayHeight, resources.displayMetrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mImageReader.surface, null, null)
        }

        private fun fromHtml(resID: Int): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) return Html.fromHtml(getString(resID), Html.FROM_HTML_MODE_COMPACT) else return Html.fromHtml(getString(resID))
    }


    }

    class SectionsPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {
        override fun getItem(p0: Int): Fragment {
            return WizardFragment.newInstance(p0)
        }

        override fun getCount(): Int {
            return 5
        }

    }
}

