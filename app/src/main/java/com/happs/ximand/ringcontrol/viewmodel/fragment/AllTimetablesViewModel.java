package com.happs.ximand.ringcontrol.viewmodel.fragment;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.happs.ximand.ringcontrol.BR;
import com.happs.ximand.ringcontrol.FragmentNavigation;
import com.happs.ximand.ringcontrol.R;
import com.happs.ximand.ringcontrol.model.mapper.impl.TimetableToTimeListMapper;
import com.happs.ximand.ringcontrol.model.object.Lesson;
import com.happs.ximand.ringcontrol.model.object.Timetable;
import com.happs.ximand.ringcontrol.model.repository.Repository;
import com.happs.ximand.ringcontrol.model.repository.impl.FakeTimetableRepository;
import com.happs.ximand.ringcontrol.model.specification.impl.GetAllSqlSpecification;
import com.happs.ximand.ringcontrol.model.writer.QueueWriter;
import com.happs.ximand.ringcontrol.view.fragment.AddTimetableFragment;
import com.happs.ximand.ringcontrol.view.fragment.TimetableInfoFragment;
import com.happs.ximand.ringcontrol.viewmodel.TimeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllTimetablesViewModel extends BaseFragmentViewModel {

    private final Repository<Timetable> timetableRepository;
    private final MutableLiveData<List<Timetable>> allTimetablesLiveData;

    private int numOfTestLessons;

    public AllTimetablesViewModel(@NonNull Application application) {
        super(application);
        this.timetableRepository = FakeTimetableRepository.getInstance();
        this.allTimetablesLiveData = new MutableLiveData<>();
        this.numOfTestLessons = 2;
        updateTimetables();
    }

    public LiveData<List<Timetable>> getAllTimetablesLiveData() {
        return allTimetablesLiveData;
    }

    @Bindable
    public int getNumOfTestLessons() {
        return numOfTestLessons;
    }

    public void setNumOfTestLessons(int numOfTestLessons) {
        this.numOfTestLessons = numOfTestLessons;
        notifyPropertyChanged(BR.numOfTestLessons);
    }

    public void updateTimetables() {
        List<Timetable> timetables = timetableRepository.query(new GetAllSqlSpecification());
        allTimetablesLiveData.setValue(timetables);
    }

    public void showTimetableDetails(Timetable timetable) {
        TimetableInfoFragment fragment = TimetableInfoFragment.newInstance(timetable);
        FragmentNavigation.getInstance().navigateToFragment(fragment);
    }

    public void applyUpdatedCurrentTimetable() {
        int appliedTimetableId = loadAppliedTimetableIdFromProperties();
        List<Timetable> allTimetables = allTimetablesLiveData.getValue();
        if (allTimetables != null) {
            applyTimetable(allTimetables.get(appliedTimetableId));
        }
    }

    public void applyTestTimetable() {
        List<Lesson> lessons = new ArrayList<>(numOfTestLessons * 2);
        for (int i = 1; i <= numOfTestLessons * 2; i+=2) {
            lessons.add(new Lesson(
                    i / 2 + 1, TimeHelper.getCurrentTimeWithFewMinutes(i),
                    TimeHelper.getCurrentTimeWithFewMinutes(i + 1)
            ));
        }
        applyTimetable(new Timetable(-1, "Test", lessons));
    }

    public void applyTimetable(Timetable timetable) {
        List<String> timeList =
                new TimetableToTimeListMapper().map(timetable);
        updateAppliedTimetableId(timetable.getId());
        //QueueWriter.getInstance().write(timeList);
    }

    public void addTestLesson() {
        setNumOfTestLessons(numOfTestLessons + 1);
    }

    public void removeTestLesson() {
        setNumOfTestLessons(numOfTestLessons - 1);
    }

    @Override
    public boolean onOptionsItemSelected(int itemId) {
        if (itemId == R.id.toolbar_add) {
            moveToAddTimetableFragment();
            return true;
        }
        return false;
    }

    public boolean isLastAppliedTimetable(Timetable timetable) {
        return timetable.getId() == loadAppliedTimetableIdFromProperties();
    }

    private void moveToAddTimetableFragment() {
        FragmentNavigation.getInstance()
                .navigateToFragment(AddTimetableFragment.newInstance());
    }
}
