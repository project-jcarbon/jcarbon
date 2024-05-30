from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class StartRequest(_message.Message):
    __slots__ = ("process_id", "period_millis")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    PERIOD_MILLIS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    period_millis: int
    def __init__(self, process_id: _Optional[int] = ..., period_millis: _Optional[int] = ...) -> None: ...

class StartResponse(_message.Message):
    __slots__ = ("response",)
    RESPONSE_FIELD_NUMBER: _ClassVar[int]
    response: str
    def __init__(self, response: _Optional[str] = ...) -> None: ...

class StopRequest(_message.Message):
    __slots__ = ("process_id",)
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    def __init__(self, process_id: _Optional[int] = ...) -> None: ...

class StopResponse(_message.Message):
    __slots__ = ("response",)
    RESPONSE_FIELD_NUMBER: _ClassVar[int]
    response: str
    def __init__(self, response: _Optional[str] = ...) -> None: ...

class DumpRequest(_message.Message):
    __slots__ = ("process_id", "output_path", "signals")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_PATH_FIELD_NUMBER: _ClassVar[int]
    SIGNALS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    output_path: str
    signals: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, process_id: _Optional[int] = ..., output_path: _Optional[str] = ..., signals: _Optional[_Iterable[str]] = ...) -> None: ...

class DumpResponse(_message.Message):
    __slots__ = ("response",)
    RESPONSE_FIELD_NUMBER: _ClassVar[int]
    response: str
    def __init__(self, response: _Optional[str] = ...) -> None: ...

class ReadRequest(_message.Message):
    __slots__ = ("process_id", "signals")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    SIGNALS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    signals: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, process_id: _Optional[int] = ..., signals: _Optional[_Iterable[str]] = ...) -> None: ...

class ReadResponse(_message.Message):
    __slots__ = ("report",)
    REPORT_FIELD_NUMBER: _ClassVar[int]
    report: JCarbonReport
    def __init__(self, report: _Optional[_Union[JCarbonReport, _Mapping]] = ...) -> None: ...

class PurgeRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class PurgeResponse(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class JCarbonReport(_message.Message):
    __slots__ = ("signal",)
    SIGNAL_FIELD_NUMBER: _ClassVar[int]
    signal: _containers.RepeatedCompositeFieldContainer[JCarbonSignal]
    def __init__(self, signal: _Optional[_Iterable[_Union[JCarbonSignal, _Mapping]]] = ...) -> None: ...

class JCarbonSignal(_message.Message):
    __slots__ = ("signal_name", "signal")
    SIGNAL_NAME_FIELD_NUMBER: _ClassVar[int]
    SIGNAL_FIELD_NUMBER: _ClassVar[int]
    signal_name: str
    signal: _containers.RepeatedCompositeFieldContainer[Signal]
    def __init__(self, signal_name: _Optional[str] = ..., signal: _Optional[_Iterable[_Union[Signal, _Mapping]]] = ...) -> None: ...

class Signal(_message.Message):
    __slots__ = ("start", "end", "component", "unit", "data")
    class Timestamp(_message.Message):
        __slots__ = ("secs", "nanos")
        SECS_FIELD_NUMBER: _ClassVar[int]
        NANOS_FIELD_NUMBER: _ClassVar[int]
        secs: int
        nanos: int
        def __init__(self, secs: _Optional[int] = ..., nanos: _Optional[int] = ...) -> None: ...
    class Data(_message.Message):
        __slots__ = ("component", "value")
        COMPONENT_FIELD_NUMBER: _ClassVar[int]
        VALUE_FIELD_NUMBER: _ClassVar[int]
        component: str
        value: float
        def __init__(self, component: _Optional[str] = ..., value: _Optional[float] = ...) -> None: ...
    START_FIELD_NUMBER: _ClassVar[int]
    END_FIELD_NUMBER: _ClassVar[int]
    COMPONENT_FIELD_NUMBER: _ClassVar[int]
    UNIT_FIELD_NUMBER: _ClassVar[int]
    DATA_FIELD_NUMBER: _ClassVar[int]
    start: Signal.Timestamp
    end: Signal.Timestamp
    component: str
    unit: str
    data: _containers.RepeatedCompositeFieldContainer[Signal.Data]
    def __init__(self, start: _Optional[_Union[Signal.Timestamp, _Mapping]] = ..., end: _Optional[_Union[Signal.Timestamp, _Mapping]] = ..., component: _Optional[str] = ..., unit: _Optional[str] = ..., data: _Optional[_Iterable[_Union[Signal.Data, _Mapping]]] = ...) -> None: ...
