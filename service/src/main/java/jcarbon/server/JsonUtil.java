package jcarbon.server;

import static java.nio.file.Files.newBufferedWriter;
import static java.util.stream.Collectors.toList;
import static jcarbon.server.LoggerUtil.getLogger;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import jcarbon.JCarbonReport;
import jcarbon.data.Interval;
import jcarbon.data.Sample;
import org.json.JSONArray;
import org.json.JSONObject;

final class JsonUtil {
  private static final Logger logger = getLogger();

  static void dump(String path, JCarbonReport report) {
    Path outputPath = Path.of(path);
    logger.info(String.format("writing reports to %s", outputPath));
    try (PrintWriter writer = new PrintWriter(newBufferedWriter(outputPath)); ) {
      writer.println(JsonUtil.toJsonReport(report));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static JSONObject toJsonReport(JCarbonReport report) {
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
