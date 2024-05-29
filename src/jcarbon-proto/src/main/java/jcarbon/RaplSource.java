// package jcarbon;

// import static java.util.stream.Collectors.toList;
// import static jcarbon.linux.CpuInfo.SOCKETS;
// import static jcarbon.util.LoggerUtil.getLogger;

// import java.time.Instant;
// import java.util.Optional;
// import java.util.concurrent.atomic.AtomicInteger;
// import java.util.function.BiFunction;
// import java.util.function.Supplier;
// import java.util.logging.Logger;
// import java.util.stream.IntStream;
// import jcarbon.linux.powercap.Powercap;
// import jcarbon.rapl.Rapl;

// /** A helper class that automatically picks an available energy source. */
// final class RaplSource {
//   private static final Logger logger = getLogger();

//   private static final RaplSource RAPL =
//       new RaplSource("/sys/devices/virtual/powercap/intel-rapl", Rapl::sample, Rapl::difference);
//   private static final RaplSource POWERCAP =
//       new RaplSource("/dev/cpu/<socket>/msr", Powercap::sample, Powercap::difference);
//   private static final RaplSource FAKE = createFakeSource();

//   /** Grab the first available energy source. Priority is rapl > powercap > fake */
//   public static RaplSource getRaplSource() {
//     logger.info("checking for a rapl source");
//     if (Powercap.isAvailable()) {
//       logger.info("found powercap!");
//       return POWERCAP;
//     } else if (Rapl.isAvailable()) {
//       logger.info("found direct rapl access!");
//       return RAPL;
//     }
//     logger.info("no energy source found! resorting to an empty fake");
//     return FAKE;
//   }

//   final String sourceName;
//   private final Supplier<Optional<RaplSample>> source;
//   private final BiFunction<RaplSample, RaplSample, RaplEnergy> differ;

//   private RaplSource(
//       String sourceName,
//       Supplier<Optional<RaplSample>> source,
//       BiFunction<RaplSample, RaplSample, RaplEnergy> differ) {
//     this.sourceName = sourceName;
//     this.source = source;
//     this.differ = differ;
//   }

//   public Optional<RaplSample> sample() {
//     return source.get();
//   }

//   public RaplEnergy difference(RaplSample first, RaplSample second) {
//     return differ.apply(first, second);
//   }

//   private static RaplSource createFakeSource() {
//     final AtomicInteger counter = new AtomicInteger(0);
//     return new RaplSource(
//         "/fake/atomic-counter",
//         () -> {
//           int value = counter.getAndIncrement();
//           return Optional.of(
//               new RaplSample(
//                   Instant.now(),
//                   IntStream.range(0, SOCKETS)
//                       .mapToObj(cpu -> forPackage(cpu, value))
//                       .collect(toList())));
//         },
//         RaplSource::sampleDifference);
//   }

//   static RaplReading forPackage(int socket, double energy) {
//     return new RaplReading(socket, energy, 0, 0, 0);
//   }

//   private static RaplEnergy sampleDifference(RaplSample first, RaplSample second) {
//     if (first.timestamp().isAfter(second.timestamp())) {
//       throw new IllegalArgumentException(
//           String.format(
//               "first sample is not before second sample (%s !< %s)",
//               first.timestamp(), second.timestamp()));
//     }
//     return new RaplEnergy(
//         first.timestamp(),
//         second.timestamp(),
//         IntStream.range(0, SOCKETS)
//             .mapToObj(
//                 socket -> Powercap.difference(first.data().get(socket), second.data().get(socket)))
//             .collect(toList()));
//   }
// }
