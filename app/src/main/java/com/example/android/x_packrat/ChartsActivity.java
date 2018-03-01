package com.example.android.x_packrat;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.x_packrat.data.BelongingsContract;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilizes code from the MPAndroidChart library to display visual representations of the user's
 * belongings data
 */
public class ChartsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, IValueFormatter {

    /*
     * References to any pie and bar charts used in this activity
     */
    private PieChart pieChart;
    private BarChart barChart;

    /*
     * Titles for bar and pie charts
     */
    private TextView barChartTitle;
    private TextView pieChartTitle;

    // Indicates how many belongings to include in the various charts
    private static final int MAX_NUM_OF_BELONGINGS_TO_SHOW = 5;

    /*
     * Projections for all the database tables we query for in this activity.
     * We query for all of them as we require data from each in every one to create our
     * various charts.
     */
    public static final String[] USAGE_LOG_PROJECTION = {
            BelongingsContract.UsageLogEntry._ID,
            BelongingsContract.UsageLogEntry.COLUMN_USAGE_DATE,
            BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID
    };
    public static final String[] BELONGINGS_PROJECTION = {
            BelongingsContract.BelongingEntry._ID,
            BelongingsContract.BelongingEntry.COLUMN_BELONGING_IMAGE,
            BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME,
            BelongingsContract.BelongingEntry.COLUMN_LAST_USED_DATE,
    };
    public static final String[] SOLD_PROJECTION = {
            BelongingsContract.SoldEntry._ID,
            BelongingsContract.SoldEntry.COLUMN_SOLD_TO,
            BelongingsContract.SoldEntry.COLUMN_BELONGING_NAME,
            BelongingsContract.SoldEntry.COLUMN_BELONGING_IMAGE
    };
    public static final String[] DISCARDED_PROJECTION = {
            BelongingsContract.DiscardedEntry._ID,
            BelongingsContract.DiscardedEntry.COLUMN_BELONGING_NAME,
            BelongingsContract.DiscardedEntry.COLUMN_BELONGING_IMAGE
    };
    public static final String[] DONATED_PROJECTION = {
            BelongingsContract.DonatedEntry._ID,
            BelongingsContract.DonatedEntry.COLUMN_DONATED_TO,
            BelongingsContract.DonatedEntry.COLUMN_BELONGING_NAME,
            BelongingsContract.DonatedEntry.COLUMN_BELONGING_IMAGE
    };

    /*
     * Populated with the data from a query for the "belongings" and "usage_log" tables.
     * The data in these is used to create our charts rather than iterating through the cursor
     * directly because the mappings organize the data and make it easier to parse.
     */
    private LinkedHashMap<String,Integer> belongingsToNumOfUses = new LinkedHashMap<>();
    private LinkedHashMap<Long,String> belongingIdToName = new LinkedHashMap<>();

    /*
     * References to the query results for the "belongings" and "usage_log" tables
     */
    Cursor mLogsCursor;
    Cursor mBelongingsCursor;

    /*
     * Are set to the number of items in each tab/fragment category after each table is queried(
     * "sold", "discarded", "donated" tables)
     */
    private int mSoldCount = -1;
    private int mDiscardedCount = -1;
    private int mDonatedCount = -1;

    /*
     * Loader IDs used to indicate which table in the database "possessions.db" to query for
     */
    private static final int ID_BELONGINGS_LOADER = 60;
    private static final int ID_USAGE_LOGS_LOADER = 61;
    private static final int ID_SOLD_LOADER = 62;
    private static final int ID_DISCARDED_LOADER = 63;
    private static final int ID_DONATED_LOADER = 64;

    // Indicates the colors to use for entries in the pie and bar charts
    public static final int[] XPACKRAT_CHART_COLORS = {
        Color.rgb(248,187,208), Color.rgb(244,143,177),
            Color.rgb(252,228,236), Color.rgb(238,238,238),
            Color.rgb(224,224,224)
    };

    // Indicates the number of labels to display on the y axis for the bar chart
    private static final int NUM_LABELS_TO_DISPLAY = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.action_charts);
        }

        pieChart = (PieChart)findViewById(R.id.belongings_pie);

        barChart = (BarChart)findViewById(R.id.belongings_bar);

        barChartTitle = (TextView)findViewById(R.id.bar_chart_title);

        pieChartTitle = (TextView) findViewById(R.id.pie_chart_title);

        pieChart.setVisibility(View.VISIBLE);

        /*
         * Initializes and starts loaders to query for the "belongings", "sold", "discarded", and
         * "donated" tables. We do not query for the "usage_log" table here because the
         * "populateNumOfUsesMap" method requires that the data from querying the "belongings" table
         * has already been inserted into the "belongingIdToName" map.
         */
        getSupportLoaderManager().initLoader(ID_BELONGINGS_LOADER, null, this);
        getSupportLoaderManager().initLoader(ID_SOLD_LOADER, null, this);
        getSupportLoaderManager().initLoader(ID_DISCARDED_LOADER, null, this);
        getSupportLoaderManager().initLoader(ID_DONATED_LOADER, null, this);
    }

    /**
     * Called when a new Loader needs to be created.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new loader instance
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        // The URI to use for querying the database
        Uri queryUri;

        // Indicates the order to return the data in the cursor
        String sortOrder;

        switch (loaderId) {

            case ID_USAGE_LOGS_LOADER:
                queryUri = BelongingsContract.UsageLogEntry.CONTENT_URI;
                sortOrder = BelongingsContract.UsageLogEntry.
                        COLUMN_USAGE_DATE + " DESC";

                return new CursorLoader(this,
                        queryUri,
                        USAGE_LOG_PROJECTION,
                        null,
                        null,
                        sortOrder);

            case ID_BELONGINGS_LOADER:
                queryUri = BelongingsContract.BelongingEntry.CONTENT_URI;
                sortOrder = BelongingsContract.BelongingEntry.
                        COLUMN_LAST_USED_DATE + " DESC";

                return new CursorLoader(this,
                        queryUri,
                        BELONGINGS_PROJECTION,
                        null,
                        null,
                        sortOrder);
            case ID_SOLD_LOADER:
                queryUri = BelongingsContract.SoldEntry.CONTENT_URI;
                sortOrder = BelongingsContract.SoldEntry.
                        COLUMN_BELONGING_NAME + " DESC";

                return new CursorLoader(this,
                        queryUri,
                        SOLD_PROJECTION,
                        null,
                        null,
                        sortOrder);
            case ID_DISCARDED_LOADER:
                queryUri = BelongingsContract.DiscardedEntry.CONTENT_URI;
                sortOrder = BelongingsContract.DiscardedEntry.
                        COLUMN_BELONGING_NAME + " DESC";

                return new CursorLoader(this,
                        queryUri,
                        DISCARDED_PROJECTION,
                        null,
                        null,
                        sortOrder);
            case ID_DONATED_LOADER:
                queryUri = BelongingsContract.DonatedEntry.CONTENT_URI;
                sortOrder = BelongingsContract.DonatedEntry.
                        COLUMN_BELONGING_NAME + " DESC";

                return new CursorLoader(this,
                        queryUri,
                        DONATED_PROJECTION,
                        null,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Called when a loader has finished loading its data.
     *
     * @param loader The loader that has finished
     * @param data   The data generated by the loader
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch(loader.getId()){
            case ID_USAGE_LOGS_LOADER:
                mLogsCursor = data;
                populateNumOfUsesMap(data);
                setPieChartProperties("most");
                break;
            case ID_BELONGINGS_LOADER:
                mBelongingsCursor = data;
                populateBelongingIdToNameMap(data);
                getSupportLoaderManager().initLoader(ID_USAGE_LOGS_LOADER, null, this);
                break;
            case ID_SOLD_LOADER:
                mSoldCount = data.getCount();
                break;
            case ID_DISCARDED_LOADER:
                mDiscardedCount = data.getCount();
                break;
            case ID_DONATED_LOADER:
                mDonatedCount = data.getCount();
                break;
        }
    }

    /**
     * Overridden as per the requirements to implement the LoaderCallbacks interface.
     *
     * @param loader The loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    /**
     * Populates the "belongingsToNumOfUses" map(using data from the "usage_log" table),
     * which is used to determine how many times the user has used a particular belonging.
     * The map is used when creating a bar chart.
     *
     * @param data The results from querying for the "usage_log" table
     */
    private void populateNumOfUsesMap(Cursor data){

        int initialNumofUses = 1;

        while (data.moveToNext()) {
            long id = data.getLong(data.getColumnIndexOrThrow(
                    BelongingsContract.UsageLogEntry.COLUMN_BELONGING_ID
            ));

            String belongingName = belongingIdToName.get(id);

            // Adds belonging to map if not already in the map, else increments the number of times
            // that belonging was used by 1
            if(!belongingsToNumOfUses.containsKey(belongingName)){
                belongingsToNumOfUses.put(belongingName,initialNumofUses);
            }
            else{
                int numUses = belongingsToNumOfUses.get(belongingName);
                belongingsToNumOfUses.put(belongingName, ++numUses);
            }
        }
    }

    /**
     * Populates the "belongingIdToName" map(using data from the "belongings" table), which is used
     * to help populate the "belongingsToNumOfUses" map.
     *
     * @param data The results from querying for the "belongings" table
     */
    private void populateBelongingIdToNameMap(Cursor data){
        while (data.moveToNext()) {
            String belongingName = data.getString(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry.COLUMN_BELONGING_NAME));
            long belongingId = data.getLong(data.getColumnIndexOrThrow(
                    BelongingsContract.BelongingEntry._ID
            ));

            belongingIdToName.put(belongingId, belongingName);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBelongingsCursor.close();
        mLogsCursor.close();
    }

    /**
     * Creates the bar chart that is displayed when the user selects this activity's "Category
     * Comparison" menu option.
     *
     */
    private void createBarChart(){

        // Case where not yet ready to show bar chart so return immediately
        if(mDiscardedCount == -1 && mSoldCount == -1 && mDonatedCount == -1){
            return;
        }

        // Hides the pie chart and displays the bar chart
        pieChart.setVisibility(View.GONE);
        barChart.setVisibility(View.VISIBLE);
        barChartTitle.setVisibility(View.VISIBLE);
        pieChartTitle.setVisibility(View.INVISIBLE);

        // Clears description text from bar chart
        Description barDescription = new Description();
        barDescription.setText("");
        barChart.setDescription(barDescription);

        // Creates all entries in the bar chart
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0,mBelongingsCursor.getCount()));
        barEntries.add(new BarEntry(1,mSoldCount));
        barEntries.add(new BarEntry(2,mDiscardedCount));
        barEntries.add(new BarEntry(3,mDonatedCount));

        // Creates labels that appear at the bottom of each bar
        ArrayList<String> tabCategories = new ArrayList<>();
        tabCategories.add(getString(R.string.belongings_tab_title));
        tabCategories.add(getString(R.string.sold_tab_title));
        tabCategories.add(getString(R.string.discarded_tab_title));
        tabCategories.add(getString(R.string.donated_tab_title));

        // Combines everything above to form the actual bar chart
        BarDataSet barDataSet = new BarDataSet(barEntries,getString(R.string.bar_chart_label));
        barDataSet.setColors(XPACKRAT_CHART_COLORS);
        barDataSet.setValueFormatter(this);
        barDataSet.setValueTextSize(14f);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        // Sets bar chart properties to the desired values
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(tabCategories));
        barChart.getXAxis().setLabelCount(barEntries.size());
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawLabels(true);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawAxisLine(true);
        barChart.getAxisLeft().setAxisMinimum(0);
        barChart.getAxisRight().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.animateY(1000);
        barChart.getLegend().setEnabled(false);

        // Helps to display int vals on y axis along with "calculateMinMax" method
        barChart.getAxisLeft().setGranularity(1.0f);
        barChart.getAxisLeft().setGranularityEnabled(true);

        barChart.setData(barData);

        calculateMinMax(barChart, NUM_LABELS_TO_DISPLAY);

        barChart.invalidate();
    }

    /**
     * Sets all pie chart properties to the desired values.
     *
     * @param usageDescriptor    Indicates whether or not the user is interested in seeing
     *                           a visual representation of their most recent or their least
     *                           recently used belongings. They indicate this based on the option
     *                           they select in this activity's menu.
     */
    private void setPieChartProperties(String usageDescriptor){

        // Hides the bar chart and makes sure the pie chart is cleared and ready to display new
        // data
        barChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        barChartTitle.setVisibility(View.GONE);
        pieChartTitle.setVisibility(View.VISIBLE);
        pieChart.clear();

        Description pieDescription = new Description();
        pieDescription.setText("");

        pieChart.setDescription(pieDescription);
        pieChart.setRotationEnabled(true);
        pieChart.setHoleRadius(25f);
        pieChart.setTransparentCircleAlpha(0);

        // Determines what text to show in the center of the pie chart
        if(usageDescriptor.equals("most")) {
            pieChart.setCenterText(getString(R.string.pie_center_text_most));
            pieChartTitle.setText(R.string.pie_chart_title_most);
        }
        else{
            pieChart.setCenterText(getString(R.string.pie_center_text_least));
            pieChartTitle.setText(R.string.pie_chart_title_least);
        }

        pieChart.setCenterTextSize(10);

        addPieDataSet(usageDescriptor);

        // Causes a toast to be displayed with information for a belonging if the user clicks on
        // a piece of the pie chart
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                String belongingName = pe.getLabel();
                float numOfUses = pe.getValue();
                String numUses = String.valueOf((int)numOfUses);

                Toast.makeText(ChartsActivity.this,belongingName + "\n" +
                getString(R.string.pie_chart_toast_text_used) + " " + numUses + " " +
                                getString(R.string.pie_chart_toast_text_times),
                                Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    /**
     * Creates the pie chart to be displayed.
     *
     * @param usageDescriptor    Indicates whether or not the user is interested in seeing
     *                           a visual representation of their most recent or their least
     *                           recently used belongings. They indicate this based on the option
     *                           they select in this activity's menu.
     */
    private void addPieDataSet(String usageDescriptor){
        ArrayList<PieEntry> yEntrys = new ArrayList<>();

        yEntrys = getPieEntries(usageDescriptor);

        PieDataSet pieDataSet;

        // Determines what text to show in the center of the pie chart
        if(usageDescriptor.equals("most")) {
            pieDataSet = new PieDataSet(yEntrys,
                    getString(R.string.pie_chart_title_text_most));
        }
        else{
            pieDataSet = new PieDataSet(yEntrys,
                    getString(R.string.pie_chart_title_text_least));
        }

        pieDataSet.setValueFormatter(this);
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);
        pieDataSet.setColors(XPACKRAT_CHART_COLORS);

        pieChart.getLegend().setEnabled(false);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(12f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setData(pieData);
        pieChart.animateY(1000);

        pieChart.invalidate();
    }

    /**
     * Inflates and sets up the menu for this Activity
     *
     * @param menu The options menu in which items are placed
     * @return True for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.charts, menu);
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu
     *
     * @param item The menu item that was selected by the user
     * @return True to indicate that menu click is handled here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_most_recent:
                setPieChartProperties("most");
                return true;
            case R.id.action_least_recent:
                setPieChartProperties("least");
                return true;
            case R.id.action_category_comparison:
                createBarChart();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets a list of max size MAX_NUM_OF_BELONGINGS_TO_SHOW of either the user's most recently
     * used belongings or their least recently used belongings.
     *
     * @param usageDescriptor    Indicates whether or not the user is interested in seeing
     *                           a visual representation of their most recent or their least
     *                           recently used belongings. They indicate this based on the option
     *                           they select in this activity's menu.
     *
     * @return list of pie entries representing the user's most recently used belongings or
     *         the user's least recently used belongings
     */
    private ArrayList<PieEntry> getPieEntries(String usageDescriptor){

        ArrayList<PieEntry> retVal = new ArrayList<>();

        int numBelongingsToShow = 0;

        // Determines whether to iterate over the "belongingsToNumUses" map in order or in
        // reverse order. This is because the entries at the beginning of the map contain
        // the most recently used belongings and the entries at the end of the map contain
        // the least recently used belongings.
        if(usageDescriptor.equals("most")){

            // Get MAX_NUM_OF_BELONGINGS_TO_SHOW of most recently used belongings
            for (Map.Entry<String,Integer> entry : belongingsToNumOfUses.entrySet()) {
                retVal.add(new PieEntry(entry.getValue(), entry.getKey()));
                ++numBelongingsToShow;

                if(numBelongingsToShow == MAX_NUM_OF_BELONGINGS_TO_SHOW){
                    break;
                }
            }
        }
        else{

            // Get MAX_NUM_OF_BELONGINGS_TO_SHOW of least recently used belongings
            List<String> reverseOrderedKeys = new ArrayList<String>(belongingsToNumOfUses.keySet());
            Collections.reverse(reverseOrderedKeys);
            for (String key : reverseOrderedKeys) {
                int numOfUses = belongingsToNumOfUses.get(key);
                retVal.add(new PieEntry(numOfUses, key));
                ++numBelongingsToShow;

                if(numBelongingsToShow == MAX_NUM_OF_BELONGINGS_TO_SHOW){
                    break;
                }
            }
        }

        return retVal;
    }

    /**
     * Used to display int values instead of float values in the charts. Formats chart float axis
     * values to string representations of ints.
     *
     * @param value            The value to convert to a formatted string
     * @param entry            The pie entry whose value will be formatted
     * @param dataSetIndex     The index of the DataSet the drawn Entry belongs to
     * @param viewPortHandler  Handler for the chart's current viewport settings
     *
     * @return a string representation of the "value" param in the desired format
     */
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                    ViewPortHandler viewPortHandler) {
        return "" + ((int) value);
    }

    /**
     * Forces bar chart to use desired label count even if the bar chart's min and max data set
     * values are close in value.
     *
     * @param chart        The bar chart
     * @param labelCount   The number of labels set to be displayed on the bar chart's Y axis
     *
     */
    private void calculateMinMax(BarLineChartBase chart, int labelCount) {
        float maxValue = chart.getData().getYMax();
        float minValue = chart.getData().getYMin();

        if ((maxValue - minValue) < labelCount) {
            float diff = labelCount - (maxValue - minValue);
            maxValue = maxValue + diff;
            chart.getAxisLeft().setAxisMaximum(maxValue);
            chart.getAxisLeft().setAxisMinimum(minValue);
        }
    }
}
