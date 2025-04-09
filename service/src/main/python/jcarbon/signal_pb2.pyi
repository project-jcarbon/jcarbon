from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class SignalInterval(_message.Message):
    __slots__ = ("start", "end", "data")
    class Timestamp(_message.Message):
        __slots__ = ("secs", "nanos")
        SECS_FIELD_NUMBER: _ClassVar[int]
        NANOS_FIELD_NUMBER: _ClassVar[int]
        secs: int
        nanos: int
        def __init__(self, secs: _Optional[int] = ..., nanos: _Optional[int] = ...) -> None: ...
    class SignalData(_message.Message):
        __slots__ = ("metadata", "value")
        class Metadata(_message.Message):
            __slots__ = ("name", "value")
            NAME_FIELD_NUMBER: _ClassVar[int]
            VALUE_FIELD_NUMBER: _ClassVar[int]
            name: str
            value: str
            def __init__(self, name: _Optional[str] = ..., value: _Optional[str] = ...) -> None: ...
        METADATA_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        metadata: _containers.RepeatedCompositeFieldContainer[SignalInterval.SignalData.Metadata]
        value: float
        def __init__(self, metadata: _Optional[_Iterable[_Union[SignalInterval.SignalData.Metadata, _Mapping]]] = ..., value: _Optional[float] = ...) -> None: ...
    START_FIELD_NUMBER: _ClassVar[int]
    END_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    start: SignalInterval.Timestamp
    end: SignalInterval.Timestamp
    data: _containers.RepeatedCompositeFieldContainer[SignalInterval.SignalData]
    def __init__(self, start: _Optional[_Union[SignalInterval.Timestamp, _Mapping]] = ..., end: _Optional[_Union[SignalInterval.Timestamp, _Mapping]] = ..., data: _Optional[_Iterable[_Union[SignalInterval.SignalData, _Mapping]]] = ...) -> None: ...

class Signal(_message.Message):
    __slots__ = ("unit", "source", "interval")
    class Unit(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
        __slots__ = ()
        UNKNOWN: _ClassVar[Signal.Unit]
        ACTIVITY: _ClassVar[Signal.Unit]
        GRAMS_OF_CO2: _ClassVar[Signal.Unit]
        JOULES: _ClassVar[Signal.Unit]
        JIFFIES: _ClassVar[Signal.Unit]
        MEGAHERTZ: _ClassVar[Signal.Unit]
        NANOSECONDS: _ClassVar[Signal.Unit]
        WATTS: _ClassVar[Signal.Unit]
        CELSIUS: _ClassVar[Signal.Unit]
        HERTZ: _ClassVar[Signal.Unit]
    UNKNOWN: Signal.Unit
    ACTIVITY: Signal.Unit
    GRAMS_OF_CO2: Signal.Unit
    JOULES: Signal.Unit
    JIFFIES: Signal.Unit
    MEGAHERTZ: Signal.Unit
    NANOSECONDS: Signal.Unit
    WATTS: Signal.Unit
    CELSIUS: Signal.Unit
    HERTZ: Signal.Unit
    UNIT_FIELD_NUMBER: _ClassVar[int]
    SOURCE_FIELD_NUMBER: _ClassVar[int]
    INTERVAL_FIELD_NUMBER: _ClassVar[int]
    unit: Signal.Unit
    source: _containers.RepeatedScalarFieldContainer[str]
    interval: _containers.RepeatedCompositeFieldContainer[SignalInterval]
    def __init__(self, unit: _Optional[_Union[Signal.Unit, str]] = ..., source: _Optional[_Iterable[str]] = ..., interval: _Optional[_Iterable[_Union[SignalInterval, _Mapping]]] = ...) -> None: ...

class Component(_message.Message):
    __slots__ = ("component_type", "component_id", "signal")
    COMPONENT_TYPE_FIELD_NUMBER: _ClassVar[int]
    COMPONENT_ID_FIELD_NUMBER: _ClassVar[int]
    SIGNAL_FIELD_NUMBER: _ClassVar[int]
    component_type: str
    component_id: str
    signal: _containers.RepeatedCompositeFieldContainer[Signal]
    def __init__(self, component_type: _Optional[str] = ..., component_id: _Optional[str] = ..., signal: _Optional[_Iterable[_Union[Signal, _Mapping]]] = ...) -> None: ...

class Report(_message.Message):
    __slots__ = ("component",)
    COMPONENT_FIELD_NUMBER: _ClassVar[int]
    component: _containers.RepeatedCompositeFieldContainer[Component]
    def __init__(self, component: _Optional[_Iterable[_Union[Component, _Mapping]]] = ...) -> None: ...
