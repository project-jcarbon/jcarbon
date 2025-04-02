// package jcarbon.linux.thermal;

// import org.apache.commons.cli.CommandLine;
// import org.apache.commons.cli.DefaultParser;
// import org.apache.commons.cli.Option;
// import org.apache.commons.cli.Options;

// final class SysThermalCooldown {
//   private static class CooldownArgs {
//     private final int periodMillis;
//     private final int targetTemperature;

//     private CooldownArgs(int periodMillis, int targetTemperature) {
//       this.periodMillis = periodMillis;
//       this.targetTemperature = targetTemperature;
//     }
//   }

//   private static final Integer DEFAULT_PERIOD_MILLIS = 1000;

//   private static CooldownArgs getCooldownArgs(String[] args) throws Exception {
//     Option periodOption =
//         Option.builder("p")
//             .hasArg(true)
//             .longOpt("period")
//             .desc("period in milliseconds to sample at")
//             .type(Integer.class)
//             .build();
//     Option temperatureOption =
//         Option.builder("t")
//             .hasArg(true)
//             .longOpt("temperature")
//             .desc("the target temperature in celsius")
//             .type(Integer.class)
//             .build();
//     Options options = new Options().addOption(periodOption).addOption(temperatureOption);
//     CommandLine cmd = new DefaultParser().parse(options, args);
//     return new CooldownArgs(
//         cmd.getParsedOptionValue(periodOption, DEFAULT_PERIOD_MILLIS).intValue(),
//         cmd.getParsedOptionValue(temperatureOption).intValue());
//   }

//   public static void main(String[] args) {
//     CooldownArgs args = getCooldownArgs(args);
//   }
// }
