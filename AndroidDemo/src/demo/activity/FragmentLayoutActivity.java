package demo.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import demo.android.R;
import demo.android.util.SystemUtil;
import demo.fragment.TitlesFragment;
import demo.fragment.activity.NavigationDrawerActivity;
import demo.fragment.activity.PropertyAnimationActivity;
import demo.fragment.activity.ViewPagerActivity;
import engine.android.util.AndroidUtil;

public class FragmentLayoutActivity extends FragmentActivity implements TitlesFragment.Callbacks {
    
    private boolean mDualPane;
    
    public static void log(String content)
    {
        System.out.println(content);
    }
	
	@Override
	protected void onCreate(Bundle arg0) {
        log("onCreate");
		super.onCreate(arg0);
		log("before setContentView()");
		setContentView(R.layout.fragment_layout_activity);
		log("after setContentView()");
		
		setupActionBar();
		
		if (findViewById(R.id.details) != null)
		{
		    ((TitlesFragment) getSupportFragmentManager().findFragmentById(R.id.titles))
		    .setListItemCheckEnabled(mDualPane = true);
		}
	}
	
	private void setupActionBar() {
	    // Set up action bar.
        final ActionBar actionBar = getActionBar();
        
        if (actionBar == null)
        {
            return;
        }

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onResume()
	{
	    log("onResume");
	    super.onResume();
	}
    
    @Override
    public void onPause() {
        log("onPause");
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        log("onDestroy");
        super.onDestroy();
    }
    
    @Override
    public void onAttachedToWindow() {
        log("onAttachedToWindow");
        super.onAttachedToWindow();
    }
    
    @Override
    public void onDetachedFromWindow() {
        log("onDetachedFromWindow");
        super.onDetachedFromWindow();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, "Navigation Drawer");
		menu.add("ViewPager").setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				startActivity(new Intent(FragmentLayoutActivity.this, ViewPagerActivity.class));
				return true;
			}
		});
		menu.add("Property Animation").setIntent(new Intent(this, PropertyAnimationActivity.class));
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (upIntent != null)
                {
                    if (NavUtils.shouldUpRecreateTask(this, upIntent))
                    {
                        // This activity is not part of the application's task, so create a new task
                        // with a synthesized back stack.
                        TaskStackBuilder.create(this)
                        // If there are ancestor activities, they should be added here.
                        .addNextIntentWithParentStack(upIntent)
                        .startActivities();
                    }
                    else
                    {
                        // This activity is part of the application's task, so simply
                        // navigate up to the hierarchical parent activity.
                        NavUtils.navigateUpTo(this, upIntent);
                    }
                }
                
                break;
            case Menu.FIRST:
                startActivity(new Intent(this, NavigationDrawerActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        
        return true;
	}
	
	public static class DetailsActivity extends FragmentActivity {
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
	    	if (SystemUtil.isTablet(this) || AndroidUtil.isLandscape(this))
	    	{
	    		finish();
	    		return;
	    	}
	    	
	    	if (savedInstanceState == null)
	    	{
	    		//initial setup
	    		DetailsFragment details = new DetailsFragment();
	    		details.setArguments(getIntent().getExtras());
	    		getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
	    	}
		}
	}
	
	public static class DetailsFragment extends Fragment {
	    
	    public static final String ARG_PISITION = "position";
        
        @Override
        public void onAttach(Activity activity)
        {
            log("onAttach");
            super.onAttach(activity);
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            log("onCreate");
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
        		Bundle savedInstanceState) {
            log("onCreateView");
        	if (container == null)
        	{
        		return null;
        	}
        	
        	ScrollView sv = new ScrollView(getActivity());
        	
        	TextView tv = new TextView(getActivity());
        	int padding = AndroidUtil.dp2px(getActivity(), 4);
        	tv.setPadding(padding, padding, padding, padding);
        	
        	tv.setText(DETAILS[getShownPosition()]);
        	
        	sv.addView(tv);
        	return sv;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            log("onActivityCreated");
            super.onActivityCreated(savedInstanceState);
        }
        
        @Override
        public void onResume() {
            log("onResume");
            super.onResume();
        }
        
        @Override
        public void onPause() {
            log("onPause");
            super.onPause();
        }
        
        @Override
        public void onDestroyView() {
            log("onDestroyView");
            super.onDestroyView();
        }
        
        @Override
        public void onDestroy() {
            log("onDestroy");
            super.onDestroy();
        }
        
        @Override
        public void onDetach() {
            log("onDetach");
            super.onDetach();
        }
		
		public int getShownPosition()
		{
			return getArguments().getInt(ARG_PISITION);
		}
        
        private void log(String content) {
            FragmentLayoutActivity.log("(" + getClass().getSimpleName() + ")" + content);
        }
	}
	
	public static final String[] TITLES =
    {
            "Henry IV (1)",
            "Henry V",
            "Henry VIII",
            "Richard II",
            "Richard III",
            "Merchant of Venice",
            "Othello",
            "King Lear"
    };
	
	public static final String[] DETAILS =
    {
            "So shaken as we are, so wan with care," +
            "Find we a time for frighted peace to pant," +
            "And breathe short-winded accents of new broils" +
            "To be commenced in strands afar remote." +
            "No more the thirsty entrance of this soil" +
            "Shall daub her lips with her own children's blood;" +
            "Nor more shall trenching war channel her fields," +
            "Nor bruise her flowerets with the armed hoofs" +
            "Of hostile paces: those opposed eyes," +
            "Which, like the meteors of a troubled heaven," +
            "All of one nature, of one substance bred," +
            "Did lately meet in the intestine shock" +
            "And furious close of civil butchery" +
            "Shall now, in mutual well-beseeming ranks," +
            "March all one way and be no more opposed" +
            "Against acquaintance, kindred and allies:" +
            "The edge of war, like an ill-sheathed knife," +
            "No more shall cut his master. Therefore, friends," +
            "As far as to the sepulchre of Christ," +
            "Whose soldier now, under whose blessed cross" +
            "We are impressed and engaged to fight," +
            "Forthwith a power of English shall we levy;" +
            "Whose arms were moulded in their mothers' womb" +
            "To chase these pagans in those holy fields" +
            "Over whose acres walk'd those blessed feet" +
            "Which fourteen hundred years ago were nail'd" +
            "For our advantage on the bitter cross." +
            "But this our purpose now is twelve month old," +
            "And bootless 'tis to tell you we will go:" +
            "Therefore we meet not now. Then let me hear" +
            "Of you, my gentle cousin Westmoreland," +
            "What yesternight our council did decree" +
            "In forwarding this dear expedience.",

            "Hear him but reason in divinity," +
            "And all-admiring with an inward wish" +
            "You would desire the king were made a prelate:" +
            "Hear him debate of commonwealth affairs," +
            "You would say it hath been all in all his study:" +
            "List his discourse of war, and you shall hear" +
            "A fearful battle render'd you in music:" +
            "Turn him to any cause of policy," +
            "The Gordian knot of it he will unloose," +
            "Familiar as his garter: that, when he speaks," +
            "The air, a charter'd libertine, is still," +
            "And the mute wonder lurketh in men's ears," +
            "To steal his sweet and honey'd sentences;" +
            "So that the art and practic part of life" +
            "Must be the mistress to this theoric:" +
            "Which is a wonder how his grace should glean it," +
            "Since his addiction was to courses vain," +
            "His companies unletter'd, rude and shallow," +
            "His hours fill'd up with riots, banquets, sports," +
            "And never noted in him any study," +
            "Any retirement, any sequestration" +
            "From open haunts and popularity.",

            "I come no more to make you laugh: things now," +
            "That bear a weighty and a serious brow," +
            "Sad, high, and working, full of state and woe," +
            "Such noble scenes as draw the eye to flow," +
            "We now present. Those that can pity, here" +
            "May, if they think it well, let fall a tear;" +
            "The subject will deserve it. Such as give" +
            "Their money out of hope they may believe," +
            "May here find truth too. Those that come to see" +
            "Only a show or two, and so agree" +
            "The play may pass, if they be still and willing," +
            "I'll undertake may see away their shilling" +
            "Richly in two short hours. Only they" +
            "That come to hear a merry bawdy play," +
            "A noise of targets, or to see a fellow" +
            "In a long motley coat guarded with yellow," +
            "Will be deceived; for, gentle hearers, know," +
            "To rank our chosen truth with such a show" +
            "As fool and fight is, beside forfeiting" +
            "Our own brains, and the opinion that we bring," +
            "To make that only true we now intend," +
            "Will leave us never an understanding friend." +
            "Therefore, for goodness' sake, and as you are known" +
            "The first and happiest hearers of the town," +
            "Be sad, as we would make ye: think ye see" +
            "The very persons of our noble story" +
            "As they were living; think you see them great," +
            "And follow'd with the general throng and sweat" +
            "Of thousand friends; then in a moment, see" +
            "How soon this mightiness meets misery:" +
            "And, if you can be merry then, I'll say" +
            "A man may weep upon his wedding-day.",

            "First, heaven be the record to my speech!" +
            "In the devotion of a subject's love," +
            "Tendering the precious safety of my prince," +
            "And free from other misbegotten hate," +
            "Come I appellant to this princely presence." +
            "Now, Thomas Mowbray, do I turn to thee," +
            "And mark my greeting well; for what I speak" +
            "My body shall make good upon this earth," +
            "Or my divine soul answer it in heaven." +
            "Thou art a traitor and a miscreant," +
            "Too good to be so and too bad to live," +
            "Since the more fair and crystal is the sky," +
            "The uglier seem the clouds that in it fly." +
            "Once more, the more to aggravate the note," +
            "With a foul traitor's name stuff I thy throat;" +
            "And wish, so please my sovereign, ere I move," +
            "What my tongue speaks my right drawn sword may prove.",

            "Now is the winter of our discontent" +
            "Made glorious summer by this sun of York;" +
            "And all the clouds that lour'd upon our house" +
            "In the deep bosom of the ocean buried." +
            "Now are our brows bound with victorious wreaths;" +
            "Our bruised arms hung up for monuments;" +
            "Our stern alarums changed to merry meetings," +
            "Our dreadful marches to delightful measures." +
            "Grim-visaged war hath smooth'd his wrinkled front;" +
            "And now, instead of mounting barded steeds" +
            "To fright the souls of fearful adversaries," +
            "He capers nimbly in a lady's chamber" +
            "To the lascivious pleasing of a lute." +
            "But I, that am not shaped for sportive tricks," +
            "Nor made to court an amorous looking-glass;" +
            "I, that am rudely stamp'd, and want love's majesty" +
            "To strut before a wanton ambling nymph;" +
            "I, that am curtail'd of this fair proportion," +
            "Cheated of feature by dissembling nature," +
            "Deformed, unfinish'd, sent before my time" +
            "Into this breathing world, scarce half made up," +
            "And that so lamely and unfashionable" +
            "That dogs bark at me as I halt by them;" +
            "Why, I, in this weak piping time of peace," +
            "Have no delight to pass away the time," +
            "Unless to spy my shadow in the sun" +
            "And descant on mine own deformity:" +
            "And therefore, since I cannot prove a lover," +
            "To entertain these fair well-spoken days," +
            "I am determined to prove a villain" +
            "And hate the idle pleasures of these days." +
            "Plots have I laid, inductions dangerous," +
            "By drunken prophecies, libels and dreams," +
            "To set my brother Clarence and the king" +
            "In deadly hate the one against the other:" +
            "And if King Edward be as true and just" +
            "As I am subtle, false and treacherous," +
            "This day should Clarence closely be mew'd up," +
            "About a prophecy, which says that 'G'" +
            "Of Edward's heirs the murderer shall be." +
            "Dive, thoughts, down to my soul: here" +
            "Clarence comes.",

            "To bait fish withal: if it will feed nothing else," +
            "it will feed my revenge. He hath disgraced me, and" +
            "hindered me half a million; laughed at my losses," +
            "mocked at my gains, scorned my nation, thwarted my" +
            "bargains, cooled my friends, heated mine" +
            "enemies; and what's his reason? I am a Jew. Hath" +
            "not a Jew eyes? hath not a Jew hands, organs," +
            "dimensions, senses, affections, passions? fed with" +
            "the same food, hurt with the same weapons, subject" +
            "to the same diseases, healed by the same means," +
            "warmed and cooled by the same winter and summer, as" +
            "a Christian is? If you prick us, do we not bleed?" +
            "if you tickle us, do we not laugh? if you poison" +
            "us, do we not die? and if you wrong us, shall we not" +
            "revenge? If we are like you in the rest, we will" +
            "resemble you in that. If a Jew wrong a Christian," +
            "what is his humility? Revenge. If a Christian" +
            "wrong a Jew, what should his sufferance be by" +
            "Christian example? Why, revenge. The villany you" +
            "teach me, I will execute, and it shall go hard but I" +
            "will better the instruction.",

            "Virtue! a fig! 'tis in ourselves that we are thus" +
            "or thus. Our bodies are our gardens, to the which" +
            "our wills are gardeners: so that if we will plant" +
            "nettles, or sow lettuce, set hyssop and weed up" +
            "thyme, supply it with one gender of herbs, or" +
            "distract it with many, either to have it sterile" +
            "with idleness, or manured with industry, why, the" +
            "power and corrigible authority of this lies in our" +
            "wills. If the balance of our lives had not one" +
            "scale of reason to poise another of sensuality, the" +
            "blood and baseness of our natures would conduct us" +
            "to most preposterous conclusions: but we have" +
            "reason to cool our raging motions, our carnal" +
            "stings, our unbitted lusts, whereof I take this that" +
            "you call love to be a sect or scion.",

            "Blow, winds, and crack your cheeks! rage! blow!" +
            "You cataracts and hurricanoes, spout" +
            "Till you have drench'd our steeples, drown'd the cocks!" +
            "You sulphurous and thought-executing fires," +
            "Vaunt-couriers to oak-cleaving thunderbolts," +
            "Singe my white head! And thou, all-shaking thunder," +
            "Smite flat the thick rotundity o' the world!" +
            "Crack nature's moulds, an germens spill at once," +
            "That make ingrateful man!"
    };

    @Override
    public void onItemSelected(int position) {
        if (mDualPane)
        {
            showDetail(position);
        }
    }

    @Override
    public void onItemClick(int position) {
        if (mDualPane)
        {
            showDetail(position);
        }
        else
        {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra(DetailsFragment.ARG_PISITION, position);
            startActivity(intent);
        }
    }
    
    private void showDetail(int position) {
        DetailsFragment details = (DetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details);
        if (details == null || details.getShownPosition() != position)
        {
            details = new DetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(DetailsFragment.ARG_PISITION, position);
            details.setArguments(bundle);
            
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.details, details)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit();
        }
    }
}