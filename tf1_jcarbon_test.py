import tempfile

import pandas as pd
import tensorflow as tf
import tensorflow.compat.v1 as tf1

from jcarbon.tensorflow.session_hook import JCarbonHook

if __name__ == '__main__':
    x_train = pd.read_csv(
        'https://storage.googleapis.com/tf-datasets/titanic/train.csv')
    x_eval = pd.read_csv(
        'https://storage.googleapis.com/tf-datasets/titanic/eval.csv')
    x_train['sex'].replace(('male', 'female'), (0, 1), inplace=True)
    x_eval['sex'].replace(('male', 'female'), (0, 1), inplace=True)

    x_train['alone'].replace(('n', 'y'), (0, 1), inplace=True)
    x_eval['alone'].replace(('n', 'y'), (0, 1), inplace=True)

    x_train['class'].replace(
        ('First', 'Second', 'Third'), (1, 2, 3), inplace=True)
    x_eval['class'].replace(('First', 'Second', 'Third'),
                            (1, 2, 3), inplace=True)

    x_train.drop(['embark_town', 'deck'], axis=1, inplace=True)
    x_eval.drop(['embark_town', 'deck'], axis=1, inplace=True)

    y_train = x_train.pop('survived')
    y_eval = x_eval.pop('survived')

    # Data setup for TensorFlow 1 with `tf.estimator`
    def _input_fn():
        return tf1.data.Dataset.from_tensor_slices((dict(x_train), y_train)).batch(32)

    def _eval_input_fn():
        return tf1.data.Dataset.from_tensor_slices((dict(x_eval), y_eval)).batch(32)

    FEATURE_NAMES = [
        'age', 'fare', 'sex', 'n_siblings_spouses', 'parch', 'class', 'alone'
    ]

    feature_columns = []
    for fn in FEATURE_NAMES:
        feat_col = tf1.feature_column.numeric_column(fn, dtype=tf.float32)
        feature_columns.append(feat_col)

    def create_sample_optimizer(tf_version):
        if tf_version == 'tf1':
            def optimizer(): return tf.keras.optimizers.legacy.Ftrl(
                l1_regularization_strength=0.001,
                learning_rate=tf1.train.exponential_decay(
                    learning_rate=0.1,
                    global_step=tf1.train.get_global_step(),
                    decay_steps=10000,
                    decay_rate=0.9))
        elif tf_version == 'tf2':
            optimizer = tf.keras.optimizers.legacy.Ftrl(
                l1_regularization_strength=0.001,
                learning_rate=tf.keras.optimizers.schedules.ExponentialDecay(
                    initial_learning_rate=0.1, decay_steps=10000, decay_rate=0.9))
        return optimizer

    model = tf.estimator.DNNEstimator(
        head=tf.estimator.BinaryClassHead(),
        feature_columns=feature_columns,
        hidden_units=[1024, 1024, 1024, 1024, 1024, 1024],
        activation_fn=tf.nn.relu,
        optimizer=create_sample_optimizer('tf1'))

    tf.compat.v1.disable_eager_execution()
    smaragdine_hook = JCarbonHook(period_ms=4, output_dir='/tmp')
    hooks = [
        smaragdine_hook,
        tf.estimator.ProfilerHook(save_steps=1, output_dir='/tmp'),
    ]

    model.train(
        input_fn=_input_fn, steps=100, hooks=hooks)
