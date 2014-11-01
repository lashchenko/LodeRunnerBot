#!/opt/local/Library/Frameworks/Python.framework/Versions/2.7/bin/python
from __future__ import print_function

# print(__doc__)

# Authors: Yann N. Dauphin, Vlad Niculae, Gabriel Synnaeve
# License: BSD

import numpy as np
import matplotlib.pyplot as plt

from numpy import savetxt, loadtxt, genfromtxt
from scipy.ndimage import convolve
from sklearn.externals import joblib
from sklearn import linear_model, datasets, metrics
from sklearn.cross_validation import train_test_split
from sklearn.neural_network import BernoulliRBM
from sklearn.pipeline import Pipeline
from os.path import exists
import sys

import logging


logging.getLogger().setLevel(logging.DEBUG)

general_path = '/Users/alashchenko/Development/Workspace/LodeRunnerBot/PyBot/'


# Load Data
classifier_dump = general_path + 'classifier_neural.dump'
if not exists(classifier_dump):
    train_csv = general_path + 'train.csv'
    train_dump = general_path + 'train.dump'
    if not exists(train_dump):
        logging.debug("Dump file {} do not exist. Try create it from {}.".format(train_dump, train_csv))
        # data = loadtxt(open(train_csv, 'r'), delimiter=',', skiprows=0)
        # data = genfromtxt(train_csv, delimiter=",", filling_values=9788)
        data = genfromtxt(train_csv, delimiter=",", invalid_raise=False)
        # data_clean = [line for line in data if ]
        logging.debug("Train data len is: {}".format(len(data)))
        joblib.dump(data, train_dump)

    logging.debug("Dump file {} exist. Try load it.".format(train_dump))
    train_set = joblib.load(train_dump)

    digits = datasets.load_digits()
    # X = np.asarray(digits.data, 'float32')
    # X, Y = nudge_dataset(X, digits.target)
    logging.debug("Split train set to target/train and start to train our classifier ...")
    target = [x[0] for x in train_set]  # action
    train = [x[1:] for x in train_set]  # board

    X = train
    Y = target

    X = (X - np.min(X, 0)) / (np.max(X, 0) + 0.0001)  # 0-1 scaling

    X_train, X_test, Y_train, Y_test = train_test_split(X, Y,
                                                        test_size=0.2,
                                                        random_state=0)

    # Models we will use
    logistic = linear_model.LogisticRegression()
    rbm = BernoulliRBM(random_state=0, verbose=True)

    classifier = Pipeline(steps=[('rbm', rbm), ('logistic', logistic)])

    ###############################################################################
    # Training

    # Hyper-parameters. These were set by cross-validation,
    # using a GridSearchCV. Here we are not performing cross-validation to
    # save time.
    rbm.learning_rate = 0.06
    rbm.n_iter = 20
    # More components tend to give better prediction performance, but larger
    # fitting time
    rbm.n_components = 100
    logistic.C = 6000.0

    # Training RBM-Logistic Pipeline
    classifier.fit(X_train, Y_train)

    # Training Logistic regression
    logistic_classifier = linear_model.LogisticRegression(C=100.0)
    logistic_classifier.fit(X_train, Y_train)

    ###############################################################################
    # Evaluation

    print()
    print("Logistic regression using RBM features:\n%s\n" % (
        metrics.classification_report(
            Y_test,
            classifier.predict(X_test))))

    print("Logistic regression using raw pixel features:\n%s\n" % (
        metrics.classification_report(
            Y_test,
            logistic_classifier.predict(X_test))))

    classifier.fit(train, target)
    logging.debug("Learning complete so create dump of classifier ...")
    joblib.dump(classifier, classifier_dump)


classifier = joblib.load(classifier_dump)
logging.debug("Predict ...")

board = sys.argv[1]
answer = classifier.predict(board.split(','))
logging.debug(" ... and answer is {}".format(answer[0]))

# exit(int(math.floor(answer[0])))
exit(int((answer[0])))


###############################################################################
# Plotting
#
# plt.figure(figsize=(4.2, 4))
# for i, comp in enumerate(rbm.components_):
#     plt.subplot(10, 10, i + 1)
#     plt.imshow(comp.reshape((8, 8)), cmap=plt.cm.gray_r,
#                interpolation='nearest')
#     plt.xticks(())
#     plt.yticks(())
# plt.suptitle('100 components extracted by RBM', fontsize=16)
# plt.subplots_adjust(0.08, 0.02, 0.92, 0.85, 0.08, 0.23)
#
# plt.show()
