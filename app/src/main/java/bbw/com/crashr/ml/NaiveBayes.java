package bbw.com.crashr.ml;

import android.util.Log;

import bbw.com.crashr.db.Incident;
import bbw.com.crashr.ml.Instance;

import java.util.*;

public class NaiveBayes
{
	private int[] mTODBins;
	private HashMap<String, Integer> mCauses;
	double[][] mConditionalTOD;
	double[][] mConditionalDOW;
	double[][] mConditionalWeather;
	double[] mPrior;
	double[] mPosterior;

	public NaiveBayes(int numTODBins)
	{
		mTODBins = new int[numTODBins];
        mCauses = new HashMap<String, Integer>();
	}

    public void train(List<Incident> trainData)
	{
		//Construct a list of the causes present in the training data
		buildCauses(trainData);

		//Compute the equal-frequency bins for the time-of-day field
		computeBins(trainData);

		//Transform the incidents into instances
		ArrayList<Instance> instances = new ArrayList<>();

		for(int i = 0; i < trainData.size(); i++)
		{
			instances.add(toInstance(trainData.get(i)));
		}

		//Build the Naive Bayes model
		mConditionalTOD = new double[mCauses.size()][mTODBins.length];
		mConditionalDOW = new double[mCauses.size()][7];
		mConditionalWeather = new double[mCauses.size()][5];
		mPrior = new double[mCauses.size()];
		mPosterior = new double[mCauses.size()];
		double numLabels = 0.0;

		for(int i = 0; i < instances.size(); i++)
		{
			Instance inst = instances.get(i);

			for(int j = 0; j < inst.causes.length; j++)
			{
				int c = inst.causes[j];

				mConditionalTOD[c][inst.timeOfDay]++;
				mConditionalDOW[c][inst.dayOfWeek]++;
				mConditionalWeather[c][inst.weather]++;
				mPrior[c]++;

				numLabels++;
			}
		}

		for(int i = 0; i < mCauses.size(); i++)
		{
			for(int j = 0; j < mTODBins.length; j++)
			{
				mConditionalTOD[i][j] /= mPrior[i];
			}

			for(int j = 0; j < 7; j++)
			{
				mConditionalDOW[i][j] /= mPrior[i];
			}

			for(int j = 0; j < 5; j++)
			{
				mConditionalWeather[i][j] /= mPrior[i];
			}

			mPrior[i] /= numLabels;
		}
	}

	public Map<String, Double> predict(Incident inc)
	{
		double total = 0.0;
        Instance inst = toInstance(inc);

		for(int i = 0; i < mPosterior.length; i++)
		{
			mPosterior[i] = mPrior[i] *
							mConditionalTOD[i][inst.timeOfDay] *
							mConditionalDOW[i][inst.dayOfWeek] *
							mConditionalWeather[i][inst.weather];

			total += mPosterior[i];
		}

		for(int i = 0; i < mPosterior.length; i++)
		{
			mPosterior[i] /= total;
		}

        Map<String, Double> ret = new HashMap<>();

        for(String s : mCauses.keySet())
        {
            ret.put(s, mPosterior[mCauses.get(s)]);
        }

        return ret;
	}

	private void buildCauses(List<Incident> trainData)
	{
		mCauses.clear();
		int causeId = 0;

		for(int i = 0; i < trainData.size(); i++)
		{
			Incident inc = trainData.get(i);

			for(int j = 0; j < inc.causes.length; j++)
			{
				if(mCauses.get(inc.causes[j]) == null)
				{
					mCauses.put(inc.causes[j], causeId);
					causeId++;
				}
			}
		}
	}

	private void computeBins(List<Incident> trainData)
	{
		//Compute time bins
		ArrayList<Long> times = new ArrayList<>();
		Calendar cal = Calendar.getInstance();

		for(int i = 0; i < trainData.size(); i++)
		{
			cal.setTime(trainData.get(i).date);
			times.add(new Long(cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)));
		}

		Collections.sort(times);

		mTODBins[0] = -1;

		for(int i = 1; i < mTODBins.length; i++)
		{
			int idx = i * (times.size() / mTODBins.length);
			mTODBins[i] = times.get(idx).intValue();
		}
	}

	private int getWeatherCategory(String weather)
	{
		switch(weather.charAt(0))
		{
			case 'F': return 0;
			case 'M': return 1;
			case 'L': return 2;
			case 'H': return 3;
			case 'S': return 4;
			default: return 0;
		}
	}

	private int findBin(int t)
	{
		for(int i = 1; i < mTODBins.length; i++)
		{
			if(t < mTODBins[i])
			{
				return i - 1;
			}
		}

		return mTODBins.length - 1;
	}

	private Instance toInstance(Incident inc)
	{
		Instance inst = new Instance();
		
		Calendar c = Calendar.getInstance();
		c.setTime(inc.date);

		//Sunday = 1, Monday = 2, ..., Saturday = 7
		inst.dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;

		//00:00 = 0, 01:00 = 60, 02:00 = 120, etc.
		inst.timeOfDay = findBin(c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE));

		//Categorical (not ordinal)
		inst.weather = getWeatherCategory(inc.weather);

		//The causes of this incident
		inst.causes = new int[inc.causes.length];

		for(int i = 0; i < inst.causes.length; i++)
		{
			inst.causes[i] = mCauses.get(inc.causes[i]);
		}

		return inst;
	}
}
