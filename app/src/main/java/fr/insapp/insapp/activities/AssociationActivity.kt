package fr.insapp.insapp.activities

import android.app.ActivityManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.analytics.FirebaseAnalytics
import fr.insapp.insapp.R
import fr.insapp.insapp.adapters.ViewPagerAdapter
import fr.insapp.insapp.fragments.EventsAssociationFragment
import fr.insapp.insapp.fragments.PostsFragment
import fr.insapp.insapp.http.ServiceGenerator
import fr.insapp.insapp.models.Association
import fr.insapp.insapp.utility.DarkenTransformation
import fr.insapp.insapp.utility.GlideApp
import fr.insapp.insapp.utility.Utils
import kotlinx.android.synthetic.main.activity_club.*

/**
 * Created by thomas on 11/11/2016.
 */

class AssociationActivity : AppCompatActivity() {

    private var association: Association? = null

    private var bgColor: Int = 0
    private var fgColor: Int = 0

    private lateinit var requestManager: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_club)

        requestManager = Glide.with(this);

        // association

        val intent = intent
        this.association = intent.getParcelableExtra("association")

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, association!!.id)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, association!!.name)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Association")
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)

        // toolbar

        setSupportActionBar(toolbar_club)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // dynamic color

        this.bgColor = Color.parseColor("#" + association!!.bgColor)
        this.fgColor = Color.parseColor("#" + association!!.fgColor)

        // collapsing toolbar

        appbar_club.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = true
            var scrollRange = -1

            override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.totalScrollRange
                }

                if (scrollRange + verticalOffset == 0) {
                    collapsing_toolbar_club.title = association!!.name
                    isShow = true

                    val upArrow = ContextCompat.getDrawable(this@AssociationActivity, R.drawable.abc_ic_ab_back_material)
                    upArrow?.setColorFilter(fgColor, PorterDuff.Mode.SRC_ATOP)
                    supportActionBar?.setHomeAsUpIndicator(upArrow)
                } else if (isShow) {
                    collapsing_toolbar_club.title = " "
                    isShow = false

                    val upArrow = ContextCompat.getDrawable(this@AssociationActivity, R.drawable.abc_ic_ab_back_material)
                    upArrow?.setColorFilter(-0x1, PorterDuff.Mode.SRC_ATOP)
                    supportActionBar?.setHomeAsUpIndicator(upArrow)
                }
            }
        })

        // dynamic color

        collapsing_toolbar_club.setContentScrimColor(bgColor)
        collapsing_toolbar_club.setStatusBarScrimColor(bgColor)

        club_profile.setBackgroundColor(bgColor)

        association_name.text = association!!.name
        association_name.setTextColor(fgColor)

        club_description_text.text = association!!.description
        club_description_text.setTextColor(fgColor)

        collapsing_toolbar_club.setCollapsedTitleTextColor(fgColor)

        requestManager
            .load(ServiceGenerator.CDN_URL + association!!.profilePicture)
            .apply(RequestOptions.circleCropTransform())
            .into(association_avatar)

        GlideApp
            .with(this)
            .load(ServiceGenerator.CDN_URL + association!!.cover)
            .transform(DarkenTransformation())
            .into(header_image_club)

        // links

        Linkify.addLinks(club_description_text, Linkify.ALL)
        Utils.convertToLinkSpan(this@AssociationActivity, club_description_text)

        // send a mail

        val email = ContextCompat.getDrawable(this@AssociationActivity, R.drawable.ic_email_black_24dp)

        if (fgColor != -0x1) {
            email?.setColorFilter(fgColor, PorterDuff.Mode.SRC_ATOP)
        } else {
            email?.setColorFilter(bgColor, PorterDuff.Mode.SRC_ATOP)
        }

        club_contact.setCompoundDrawablesWithIntrinsicBounds(email, null, null, null)
        club_contact.setOnClickListener { sendEmail() }

        // view pager

        setupViewPager(viewpager_club, bgColor)

        if (fgColor != -0x1) {
            setupViewPager(viewpager_club, fgColor)
        } else {
            setupViewPager(viewpager_club, bgColor)
        }

        // tab layout

        tabs_club.setupWithViewPager(viewpager_club)
        tabs_club.setBackgroundColor(bgColor)

        if (fgColor == -0x1) {
            tabs_club.setTabTextColors(-0x242425, fgColor)
        } else {
            tabs_club.setTabTextColors(-0xa1a1a2, fgColor)
        }

        // recent apps system UI

        val title = getString(R.string.app_name)
        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        setTaskDescription(ActivityManager.TaskDescription(title, icon, bgColor))
    }

    private fun setupViewPager(viewPager: ViewPager, swipeColor: Int) {
        val adapter = ViewPagerAdapter(supportFragmentManager)

        val postsFragment = PostsFragment()
        val bundle1 = Bundle()
        bundle1.putInt("layout", R.layout.post)
        bundle1.putString("filter_association_id", association!!.id)
        bundle1.putInt("swipe_color", swipeColor)
        postsFragment.arguments = bundle1
        adapter.addFragment(postsFragment, resources.getString(R.string.posts))

        val eventsClubFragment = EventsAssociationFragment()
        val bundle2 = Bundle()
        bundle2.putInt("layout", R.layout.row_event)
        bundle2.putString("filter_association_id", association!!.id)
        bundle2.putInt("swipe_color", swipeColor)
        bundle2.putParcelable("association", association)
        eventsClubFragment.arguments = bundle2
        adapter.addFragment(eventsClubFragment, resources.getString(R.string.events))

        viewPager.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO)

        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(association!!.email))
        intent.putExtra(Intent.EXTRA_SUBJECT, "")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}