package de.codebucket.mkkm.activity

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem

import androidx.appcompat.app.AppCompatActivity

import de.codebucket.mkkm.BuildConfig
import de.codebucket.mkkm.R
import de.codebucket.mkkm.databinding.ActivityAboutBinding

import me.jfenn.attribouter.attribouterFragment

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setTitle(R.string.attribouter_title_about)

        val fragment = attribouterFragment {
            withFile(R.xml.attribouter)
            withTheme(R.style.AttribouterTheme_DayNight)
            withGitHubToken(BuildConfig.GITHUB_TOKEN)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment, fragment).commit()
        } else {
            supportFragmentManager.beginTransaction().replace(R.id.fragment, fragment).commit()
        }

        // Enable back arrow button to return
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(Bundle())
    }
}
