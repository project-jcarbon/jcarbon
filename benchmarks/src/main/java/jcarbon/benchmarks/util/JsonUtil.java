package jcarbon.benchmarks.util;

import static java.util.stream.Collectors.toList;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import jcarbon.JCarbonReport;
import jcarbon.data.Interval;
import jcarbon.data.Sample;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonUtil {
  public static JSONObject toJsonReport(JCarbonReport report) {
    JSONObject jsonReport = new JSONObject();
    report
        .getSignals()
        .forEach(
            (signalType, signal) ->
                jsonReport.put(
                    signalType.getSimpleName(),
                    new JSONArray(signal.stream().map(JsonUtil::encodeToJson).collect(toList()))));
    return jsonReport;
  }

  public static JSONArray toJsonReports(Collection<JCarbonReport> reports) {
    return new JSONArray(reports.stream().map(JsonUtil::toJsonReport).collect(toList()));
  }

  private static JSONObject encodeToJson(Object o) {
    if (Sample.class.isInstance(o)) {
      return encodeSample((Sample) o);
    }
    if (Interval.class.isInstance(o)) {
      return encodeInterval((Interval) o);
    }
    return new JSONObject();
  }

  private static JSONObject encodeSample(Sample sample) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("timestamp", encodeTimestamp(sample.timestamp()));
    jsonObject.put("component", sample.component());
    jsonObject.put("data", encodeData(sample.data()));
    return jsonObject;
  }

  private static JSONObject encodeInterval(Interval interval) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("start", encodeTimestamp(interval.start()));
    jsonObject.put("end", encodeTimestamp(interval.end()));
    jsonObject.put("component", interval.component());
    jsonObject.put("data", encodeData(interval.data()));
    return jsonObject;
  }

  private static JSONObject encodeTimestamp(Instant timestamp) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("seconds", timestamp.getEpochSecond());
    jsonObject.put("nanos", timestamp.getNano());
    return jsonObject;
  }

  private static Object encodeData(Object data) {
    if (Double.class.isInstance(data)) {
      return data;
    } else if (data.getClass().isArray()) {
      JSONArray jsonData = new JSONArray();
      Arrays.stream(((Object[]) data)).forEach(jsonData::put);
      return jsonData;
    } else if (List.class.isInstance(data)) {
      JSONArray jsonData = new JSONArray();
      ((List<Object>) data).forEach(jsonData::put);
      return jsonData;
    } else {
      return new JSONArray(data);
    }
  }

  private JsonUtil() {}
}
